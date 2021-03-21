
public class Values {
    
    /**
        kaitenritsu : 回転率
        round_base : 1Rあたりの出玉
     */

    static double kaitenritu = 17.3;
    static double round_base = 110;
    
    // いまの台のボーダーを計算する
    static double Calc_my_border(Spec_Musou3 sp){
        
        double round = sp.round_num();
        System.out.println("平均ラウンド数: " + round);
        System.out.println("等価ボーダー: " + sp.getTouka_border());
        double my_dedama = round_base*round;
        System.out.println("-----今の台の数値-----");
        System.out.println("回転率: " + kaitenritu);
        System.out.println("ラウンドベース: " + round_base);
        double tmp = sp.jack_prob*(1-sp.jack_prob_yutime) + sp.jack_prob_denchu*sp.jack_prob_yutime;
        System.out.println("今の台のボーダー: " + tmp / my_dedama * 250);
        
        return tmp / my_dedama * 250;
    }
    
    // -------------時給計算----------------
    static double Calc_money_per_hour(Spec_Musou3 sp){
        
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