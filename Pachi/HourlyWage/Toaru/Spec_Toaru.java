

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

public class Spec_Toaru {

    /**
        jack_prob : 大当たり確率の分母（1/99なら99）
        normal_prob : 通常が当たる実質確率
        kakuhen_prob: 確変が当たる実質確率
        chain_ave : 確変時の平均連チャン数
        tenjou : 遊タイム天井ゲーム数
        game_yutime : 遊タイムゲーム数
        round_num_ave : 大当たり1回あたりの平均ラウンド数
     **/
    
    double jack_prob = 319.6;
    double normal_prob = 0;
    double kakuhen_prob = 100;
    double chain_ave = 4.76;
    double tenjou = 800;
    double game_yutime = 1214;

    // 確率jack_probのものがnum_game回転ハマる確率
    double through_prob(double jack_prob, double num_game){
        return Math.pow((jack_prob-1)/jack_prob, num_game);
    }

    // 4Rの時は出玉が360玉でそれは30％で起こる、という情報を{4, {360, 30}}という形で持つ
    // NBはNormal Bonusでヘソでの当たりを指す
    double NB_4R_prob = 1.0;

    Map<Integer, Pair<Integer, Double>> NB = new HashMap<>();
    Pair<Integer, Double> R4 = new Pair<>(360, NB_4R_prob);
    
    void NB_init(){
        this.NB.put(4, R4);
    }

    // DBはDnchu Bonusで電チュー時の当たりを指す
    double DB_4R_prob = 0.30;
    double DB_10R_prob = 0.70;

    Map<Integer, Pair<Integer, Double>> DB = new HashMap<>();
    Pair<Integer, Double> DR4 = new Pair<>(360, DB_4R_prob);
    Pair<Integer, Double> DR10 = new Pair<>(1400, DB_10R_prob);
    
    void DB_init(){
        this.DB.put(4, DR4);
        this.DB.put(10, DR10);
    }

    // 大当たり1回あたりの平均ラウンド数
    double round_num(){
        
        // 確変中の平均ラウンド数
        double round_num_kakuhen = 0;
        for(Integer round : DB.keySet()){
            round_num_kakuhen += (double)round * DB.get(round).second();
        }

        // 通常時の平均ラウンド数
        double round_num_normal = 0;
        for(Integer round : NB.keySet()){
            round_num_normal += (double)round * NB.get(round).second();
        }
        //System.out.println("平均ラウンド数: " + (round_num_normal + round_num_kakuhen * (chain_ave-1)));
        return (round_num_normal + round_num_kakuhen * (chain_ave-1));
    }

    // 確変時の1回あたりの期待出玉
    // 例:　4R 400個が30％、10R 1000個が70％なら400*30/100 + 1000*70/100
    double dedama_per_kakuhen = 0;

    void Calc_dedama_per_kakuhen(){

        for(Integer round : DB.keySet()){
            dedama_per_kakuhen += DB.get(round).first()* DB.get(round).second();
        }
        dedama_per_kakuhen *= (chain_ave-1);
        dedama_per_kakuhen += NB.get(4).first();
        System.out.println("期待出玉: " + dedama_per_kakuhen);
    }

    // 大当たり1回あたりの期待出玉
    // 通常50％確変50％だが時短30回込みで通常36.87％確変73.13％
    // 1. 時短30回で1/99を引かない確率を出す→Aとする (A: 73.74％)
    // 2. A*50/100が通常を引いてそのまま時短を駆け抜ける確率 (=36.87％)
    double getDedama_per_jack(){
        return (dedama_per_kakuhen * kakuhen_prob)/100;
    }

    //遊タイム中に当たる確率
    double jack_prob_inyutime = 1-through_prob(jack_prob, game_yutime);

    //遊タイムまでに当たらない確率
    double notjack_prob = through_prob(jack_prob, tenjou);

    //0回転からスタートして遊タイム中に当たる確率
    double jack_prob_yutime = jack_prob_inyutime * notjack_prob;

    // 大当たり確率1/99
    // 1/99を当てるために平均何発使うのかを求めたい
    // 1玉で何回当てられるのかを計算し、それに250をかければ等価ボーダーが出る
    double getJack_per_tama(){
        double tmp = jack_prob*(1-jack_prob_yutime) + jack_prob*jack_prob_yutime*tenjou/(tenjou+game_yutime);
        //System.out.println("実質的な大当たり確率 : " + tmp);
        //double tmp = getDedama_per_jack()*(1-jack_prob_yutime) + getDedama_per_jack()*jack_prob_yutime*(tenjou+game_yutime)/tenjou;
        //System.out.println("推定消費出玉: " + getDedama_per_jack()*(1-jack_prob_yutime) + getDedama_per_jack()*jack_prob_yutime*(tenjou+game_yutime)/tenjou);
        //return jack_prob / tmp;
        //return jack_prob / getDedama_per_jack();
        return tmp / getDedama_per_jack();
    }
    double getTouka_border(){
        return getJack_per_tama() * 250;
    }
}
