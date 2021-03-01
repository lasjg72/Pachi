

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

public class Spec {
    
    /**
        jack_prob : 大当たり確率の分母（1/99なら99）
        normal_prob : 通常が当たる実質確率
        kakuhen_prob: 確変が当たる実質確率
        chain_ave : 確変時の平均連チャン数
        tenjou : 遊タイム天井ゲーム数
        game_yutime : 遊タイムゲーム数
     **/

    double jack_prob = 99;
    double normal_prob = 36.87;
    double kakuhen_prob = 73.13;
    double chain_ave = 3.03;
    double tenjou = 0;
    double game_yutime = 1214;

    // 確率jack_probのものがnum_game回転ハマる確率
    double through_prob(double jack_prob, double num_game){
        return Math.pow((jack_prob-1)/jack_prob, num_game);
    }

    // 4Rの時は出玉が360玉でそれは30％で起こる、という情報を{4, {360, 30}}という形で持つ
    // NBはNormal BonusでRank Up Bonus以外の当たりを指す
    double NB_4R_prob = 1.0;
    double NB_5R_prob = 0.25;
    double NB_10R_prob = 0.50;

    Map<Integer, Pair<Integer, Double>> NB = new HashMap<>();
    Pair<Integer, Double> R4 = new Pair<>(360, NB_4R_prob);
    Pair<Integer, Double> R5 = new Pair<>(450, NB_5R_prob);
    Pair<Integer, Double> R10 = new Pair<>(900, NB_10R_prob);
    
    void NB_init(){
        this.NB.put(4, R4);
        this.NB.put(5, R5);
        this.NB.put(10, R10);
    }
    
    //RUBはRank Up Bonus
    double RUB_4R_prob = 0.15;
    double RUB_5R_prob = 0.055;
    double RUB_6R_prob = 0.025;
    double RUB_8R_prob = 0.01;
    double RUB_10R_prob = 0.01;
    Map<Integer, Pair<Integer, Double>> RUB = new HashMap<>();
    Pair<Integer, Double> RUB4 = new Pair<>(280, RUB_4R_prob);
    Pair<Integer, Double> RUB5 = new Pair<>(370, RUB_5R_prob);
    Pair<Integer, Double> RUB6 = new Pair<>(460, RUB_6R_prob);
    Pair<Integer, Double> RUB8 = new Pair<>(640, RUB_8R_prob);
    Pair<Integer, Double> RUB10 = new Pair<>(820, RUB_10R_prob);
    
    void RUB_init(){
        this.RUB.put(4, RUB4);
        this.RUB.put(5, RUB5);
        this.RUB.put(6, RUB6);
        this.RUB.put(8, RUB8);
        this.RUB.put(10, RUB10);
    }

    // 確変時の1回あたりの期待出玉
    // 例:　4R 400個が30％、10R 1000個が70％なら400*30/100 + 1000*70/100
    double dedama_per_kakuhen = 0;

    void Calc_dedama_per_kakuhen(){

        for(Integer round : NB.keySet()){
            if(round.equals(4)) continue;
            dedama_per_kakuhen += NB.get(round).first()* NB.get(round).second();
        }
        for(Integer round : RUB.keySet()){
            dedama_per_kakuhen +=  RUB.get(round).first()* RUB.get(round).second();
        }
        dedama_per_kakuhen *= (chain_ave-1);
        dedama_per_kakuhen += NB.get(4).first();

    }

    // 通常時の1回あたりの期待出玉
    double getDedama_per_normal(){
        return NB.get(4).first();
    }

    // 大当たり1回あたりの期待出玉
    // 通常50％確変50％だが時短30回込みで通常36.87％確変73.13％
    // 1. 時短30回で1/99を引かない確率を出す→Aとする (A: 73.74％)
    // 2. A*50/100が通常を引いてそのまま時短を駆け抜ける確率 (=36.87％)
    double getDedama_per_jack(){
        return (getDedama_per_normal() * normal_prob + dedama_per_kakuhen * kakuhen_prob)/100;
    }
    // 大当たり確率1/99
    // 1/99を当てるために平均何発使うのかを求めたい
    // 1玉で何回当てられるのかを計算し、それに250をかければ等価ボーダーが出る
    double getJack_per_tama(){
        return jack_prob / getDedama_per_jack();
    }
    double getTouka_border(){
        return getJack_per_tama() * 250;
    }
}
