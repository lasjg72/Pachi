package Musou;


/**
    遊タイムを考慮した期待値算出ツール
    期待値算出の大枠は以下の通り
    （通常時の大当たり1回での期待出玉）-（通常時に大当たりを引くまでの消費出玉）
    
    後者の算出方法は以下の通り。
    仮に大当たり確率1/319.6、低確率800回転消化で1214回転の遊タイムに入る機種があるとする。
    遊タイム中に当たる確率は (1-((319.6-1)/319.6)^1214)* 100 = 97.7729%
    遊タイムまでに当たらない確率は ((319.6-1)/319.6)^800 * 100 = 8.15081%
    0回転からスタートして800回転ハマり、遊タイム中に当たる確率は0.977729 * 0.0815081 * 100 = 7.96928%
    つまり約8%の確率で実質的に消費出玉を節約できている。
    全体の初当たりのうち92%は通常の消費出玉、8%は800+1214回転をほぼ800回転の消費出玉で回せる。
    1玉で何回当てられるのか、を計算しているjack_per_tamaの部分が92%はそのままだが8%は(800+1214)/800倍当たりやすくなっている

 **/

import java.util.Map;
import java.util.HashMap;

class Pair<T, U> {
    public T value1;
    public U value2;

    Pair(T x, U y) {
      value1 = x;
      value2 = y;
    }
  
    T first() { return value1; }
    U second() { return value2; }
    void setFirst(T x) { value1 = x; }
    void setSecond(U y) { value2 = y; }

}

public class Spec_Musou {

    /**
        jack_prob : 大当たり確率の分母（1/99なら99）
        jack_prob_denchu : 電チューでの大当たり確率
        normal_prob : 通常が当たる実質確率
        kakuhen_prob: 確変が当たる実質確率
        chain_ave : 確変時の平均連チャン数
     **/
    
    double jack_prob = 319.7;
    double jack_prob_denchu = 81.2;
    double normal_prob = through_prob(319.7, 100)*0.5*100;
    double kakuhen_prob = (1-normal_prob/100)*100;
    double chain_ave = 1/(1-(1-through_prob(81.2, 130)));

    // 確率jack_probのものがnum_game回転ハマる確率
    double through_prob(double jack_prob, double num_game){
        return Math.pow((jack_prob-1)/jack_prob, num_game);
    }

    // 4Rの時は出玉が360玉でそれは30％で起こる、という情報を{4, {360, 30}}という形で持つ
    // NBはNormal Bonusでヘソでの当たりを指す
    double NB_6R_prob = 1.0;

    Map<Integer, Pair<Integer, Double>> NB = new HashMap<>();
    Pair<Integer, Double> R6 = new Pair<>(840, NB_6R_prob);
    
    void NB_init(){
        this.NB.put(6, R6);
    }

    // DBはDnchu Bonusで電チュー時の当たりを指す
    double DB_4R_prob = 0.42;
    double DB_8R_prob = 0.07;
    double DB_16R_prob = 0.51;
    
	
    Map<Integer, Pair<Integer, Double>> DB = new HashMap<>();
    Pair<Integer, Double> DR4 = new Pair<>(530, DB_4R_prob);
    Pair<Integer, Double> DR8 = new Pair<>(1130, DB_8R_prob);
    Pair<Integer, Double> DR16 = new Pair<>(2320, DB_16R_prob);
    
    void DB_init(){
        this.DB.put(4, DR4);
        this.DB.put(8, DR8);
        this.DB.put(16, DR16);
    }

    // 大当たり1回あたりの平均ラウンド数
    double round_num(){
        
        // 確変中の1回の大当たりの平均ラウンド数
        double round_num_kakuhen = 0;
        for(Integer num : DB.keySet()){
            round_num_kakuhen += (double)num * DB.get(num).second();
        }
        
        // 通常時の1回の大当たりの平均ラウンド数
        double round_num_normal = 0;
        for(Integer num : NB.keySet()){
            round_num_normal += (double)num * NB.get(num).second();
        }
        double round = (round_num_kakuhen*(chain_ave-1)+round_num_normal)*kakuhen_prob/100+round_num_normal*normal_prob/100;
        return round;
    }
    
    // 確変時の1回あたりの期待出玉
    // 例:　4R 400個が30％、10R 1000個が70％なら400*30/100 + 1000*70/100
    double dedama_per_kakuhen = 0;
    double dedama_per_kakuhen1kai = 0;
    
    void Calc_dedama_per_kakuhen(){

        for(Integer round : DB.keySet()){
            dedama_per_kakuhen += DB.get(round).first()* DB.get(round).second();
        }
        dedama_per_kakuhen1kai = dedama_per_kakuhen;
        dedama_per_kakuhen *= (chain_ave-1);
        dedama_per_kakuhen += NB.get(6).first();
    }

    /**
        1回の初当たりでの期待出玉を計算する。
        計算の大枠は以下の通り
        （遊タイム以外での初当たりの期待出玉）＋（遊タイム中での初当たりの期待出玉）

        前者について、kakuhen_prob は時短4回に突入した時の確変確率であるので直撃の12.5％は
        含まれていない。
        よってまずは 87.5％で確変もしくは通常が当たるのでその期待出玉を求め、その後
        12.5％で確変が当たるのでその期待出玉との和が前者の値となる。

        後者については上記の Calc_dedama_per_kakuhen_yutime() を用いればよい。

        前者が起こる確率は 1-jack_prob_yutime、後者が起こる確率は jack_prob_yutimeである。
     */

    double getDedama_per_jack(){
    	double dedama_tyokugekikomi = ((dedama_per_kakuhen+ dedama_per_kakuhen1kai) * (1-through_prob(319.7, 100)) + NB.get(6).first()*normal_prob/100)*0.5+dedama_per_kakuhen*0.5;
    	return dedama_tyokugekikomi; 
    }

    // 大当たり確率1/99
    // 1/99を当てるために平均何発使うのかを求めたい
    // 1玉で何回当てられるのかを計算し、それに250をかければ等価ボーダーが出る
    double getJack_per_tama(){
        double tmp = 319.7;
        System.out.println("1Rあたりの期待出玉 "+ getDedama_per_jack()/round_num());
        System.out.println("1回の初当たり時の期待出玉: " + getDedama_per_jack());
        return tmp / getDedama_per_jack();
    }

    double getTouka_border(){
        return getJack_per_tama() * 250;
    }
}