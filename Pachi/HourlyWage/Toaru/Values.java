
public class Values {
    //回転率
    static double kaitenritu = 17.3;
    static double round_base = 134.6;
    // いまの台のボーダーを計算する
    static double Calc_my_border(Spec_Toaru sp){
        double round = sp.round_num();
        double my_dedama = round_base*round;
        System.out.println("回転率: " + kaitenritu);
        //System.out.println("ラウンドベースのボーダー: " + ((sp.dedama_per_kakuhen * sp.kakuhen_prob)/100)/round);
        double tmp = sp.jack_prob*(1-sp.jack_prob_yutime) + sp.jack_prob*sp.jack_prob_yutime*sp.tenjou/(sp.tenjou+sp.game_yutime);
        System.out.println("この台の等価ボーダー: " + sp.getTouka_border());
        System.out.println("自分のボーダー: " + tmp / my_dedama * 250);
        return tmp / my_dedama * 250;
    }
    
    // -------------時給計算----------------
    static double Calc_money_per_hour(Spec_Toaru sp){
        
        // 通常時の時速
        int speed = 180;

        // 等価ボーダーの回転率で1時間をどのくらいの消費出玉で回せるのかを計算する
        // speedから1時間あたりの回転数が分かっているので等価ボーダーと組み合わせて1時間あたりの消費出玉は speed / (touka_border / 250)
        // 上の式の意味は単位で考えると分かりやすい　(回転数/1時間)/((回転数/1k)/(消費玉/1k)) = (回転数/1時間)/(回転数/消費玉) = 消費玉/1時間
        double border = Calc_my_border(sp);
        double use_per_hour = speed / (border / 250);

        // 等価ボーダーでの消費出玉を出せたので、続いていまの台での消費出玉を計算する
        double nowuse_per_hour = speed / (kaitenritu / 250);

        //いまの台での1時間あたりの消費出玉と、ボーダーでの消費出玉が計算できたので、その差が時給となる
        double money_per_hour = use_per_hour - nowuse_per_hour;
        return money_per_hour;
    }
}
