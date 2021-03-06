

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

public class Spec_Musou3 {

    /**
        jack_prob : 大当たり確率の分母（1/99なら99）
        jack_prob_denchu : 電チューでの大当たり確率
        normal_prob : 通常が当たる実質確率
        kakuhen_prob: 確変が当たる実質確率
        chain_ave : 確変時の平均連チャン数
        tenjou : 遊タイム天井ゲーム数
        game_yutime : 遊タイムゲーム数
     **/
    
    double jack_prob = 319.7;
    double jack_prob_denchu = 6.17;
    double normal_prob = through_prob(6.17, 4)*0.875*100;
    double kakuhen_prob = (1-normal_prob/100)*100;
    double chain_ave = 1/(1-(1-through_prob(6.17, 13)));
    double tenjou = 949;
    double game_yutime = 255;

    // 確率jack_probのものがnum_game回転ハマる確率
    double through_prob(double jack_prob, double num_game){
        return Math.pow((jack_prob-1)/jack_prob, num_game);
    }

    // 4Rの時は出玉が360玉でそれは30％で起こる、という情報を{4, {360, 30}}という形で持つ
    // NBはNormal Bonusでヘソでの当たりを指す
    double NB_3R_prob = 1.0;

    Map<Integer, Pair<Integer, Double>> NB = new HashMap<>();
    Pair<Integer, Double> R3 = new Pair<>(360, NB_3R_prob);
    
    void NB_init(){
        this.NB.put(3, R3);
    }

    // DBはDnchu Bonusで電チュー時の当たりを指す
    double DB_4R_prob = 0.50;
    double DB_7R_prob = 0.25;
    double DB_10R_prob = 0.25;
	
    Map<Integer, Pair<Integer, Double>> DB = new HashMap<>();
    Pair<Integer, Double> DR4 = new Pair<>(380, DB_4R_prob);
    Pair<Integer, Double> DR7 = new Pair<>(740, DB_7R_prob);
    Pair<Integer, Double> DR10 = new Pair<>(1100, DB_10R_prob);
    
    void DB_init(){
        this.DB.put(4, DR4);
        this.DB.put(7, DR7);
        this.DB.put(10, DR10);
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
       
        //遊タイム中以外で大当たりした場合の平均ラウンド数
        double round_num_outyutime = round_num_normal*normal_prob/100 + (round_num_kakuhen* (chain_ave-1)+round_num_normal)*kakuhen_prob/100;
        
        //遊タイム中に大当たりした場合の平均ラウンド数
        double round_num_inyutime = round_num_kakuhen*(chain_ave-1)+round_num_kakuhen;

        double round = round_num_outyutime*(1-jack_prob_yutime)+round_num_inyutime*jack_prob_yutime;
        return round;
    }
    
    // 確変時の1回あたりの期待出玉
    // 例:　4R 400個が30％、10R 1000個が70％なら400*30/100 + 1000*70/100
    double dedama_per_kakuhen = 0;

    void Calc_dedama_per_kakuhen(){

        for(Integer round : DB.keySet()){
            dedama_per_kakuhen += DB.get(round).first()* DB.get(round).second();
        }
        dedama_per_kakuhen *= (chain_ave-1);
        dedama_per_kakuhen += NB.get(3).first();
    }
    
    /**
        遊タイムで初当たりを引くと電チューでの当たりとなるので
        上で求めた dedama_per_kakuhen が遊タイム中での初当たりの期待出玉となりそうだが、
        確変時の期待出玉は1回目の当たりをヘソで当てた前提となっている。
        （ Calc_dedama_per_kakuhen() 内の最終行で1回目の3R当たりを加算している）
        上記の仮定から1回目の当たりから電チューで当たった値を計算する必要がある。
     */
    double Calc_dedama_per_kakuhen_yutime(){
    	double dedama_yutime = 0;
        for(Integer round : DB.keySet()){
        	dedama_yutime += DB.get(round).first()* DB.get(round).second();
        }
        dedama_yutime *= (chain_ave);
        return dedama_yutime;
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
    	double dedama_tyokugekikomi = ((dedama_per_kakuhen * (1-through_prob(6.17, 4)) + NB.get(3).first()*normal_prob/100)*0.875+dedama_per_kakuhen*0.125);
        return dedama_tyokugekikomi*(1-jack_prob_yutime)+(Calc_dedama_per_kakuhen_yutime()*jack_prob_yutime);    
    }

    //遊タイム中に当たる確率
    double jack_prob_inyutime = 1-through_prob(jack_prob_denchu, game_yutime);

    //遊タイムまでに当たらない確率
    double notjack_prob = through_prob(jack_prob, tenjou);

    //0回転からスタートして遊タイム中に当たる確率
    double jack_prob_yutime = jack_prob_inyutime * notjack_prob;

    // 大当たり確率1/99
    // 1/99を当てるために平均何発使うのかを求めたい
    // 1玉で何回当てられるのかを計算し、それに250をかければ等価ボーダーが出る
    double getJack_per_tama(){
        double tmp = jack_prob*(1-jack_prob_yutime) + 6.17*jack_prob_yutime;
        System.out.println("1Rあたりの期待出玉 "+ getDedama_per_jack()/round_num());
        System.out.println("1回の初当たり時の期待出玉: " + getDedama_per_jack());
        return tmp / getDedama_per_jack();
    }

    double getTouka_border(){
        return getJack_per_tama() * 250;
    }
}