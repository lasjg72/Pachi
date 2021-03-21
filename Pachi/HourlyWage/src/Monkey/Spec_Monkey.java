package Monkey;

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

	public class Spec_Monkey {

	    /**
        jack_prob : 大当たり確率の分母（1/99なら99）
        normal_prob : 通常が当たる実質確率
        kakuhen_prob: 確変が当たる実質確率
        chain_ave : 確変時の平均連チャン数
        tenjou : 遊タイム天井ゲーム数
        game_yutime : 遊タイムゲーム数
        round_num_ave : 大当たり1回あたりの平均ラウンド数
     **/
	    
	    double jack_prob = 199.20;
	    double jack_prob_denchu = 7.67;
	    double normal_prob = through_prob(7.67, 5)*0.96*100;
	    double kakuhen_prob = (1-normal_prob/100)*100;
	    double chain_ave = 1/(1-(1-((through_prob(7.67, 11))*0.93)+(through_prob(7.67, 255))*0.07));
	    double tenjou = 500;
	    double game_yutime = 255;

	    // 確率jack_probのものがnum_game回転ハマる確率
	    double through_prob(double jack_prob, double num_game){
	        return Math.pow((jack_prob-1)/jack_prob, num_game);
	    }

	    // 4Rの時は出玉が360玉でそれは30％で起こる、という情報を{4, {360, 30}}という形で持つ
	    // NBはNormal Bonusでヘソでの当たりを指す
	    double NB_4R_prob = 0.96;

	    Map<Integer, Pair<Integer, Double>> NB = new HashMap<>();
	    Pair<Integer, Double> R4 = new Pair<>(400, NB_4R_prob);
	    
	    double NB_10R_prob = 0.04;

	    Pair<Integer, Double> R10 = new Pair<>(1000, NB_10R_prob);
	    
	    void NB_init(){
	        this.NB.put(4, R4);
	        this.NB.put(10, R10);
	    }

	    

	    // DBはDnchu Bonusで電チュー時の当たりを指す
	    double DB_3R_prob = 0.3;
	    double DB_10R_prob = 0.7;
		
	    Map<Integer, Pair<Integer, Double>> DB = new HashMap<>();
	    Pair<Integer, Double> DR3 = new Pair<>(300, DB_3R_prob);
	    Pair<Integer, Double> DR10 = new Pair<>(1000, DB_10R_prob);
	    
	    void DB_init(){
	        this.DB.put(3, DR3);
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
	        //dedama_per_kakuhen += NB.get(4).first()*0.96+ NB.get(10).first()*0.04;
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
	    	double dedama_tyokugekikomi = (((dedama_per_kakuhen+NB.get(4).first()) * (1-through_prob(7.67, 5)) + NB.get(4).first()*normal_prob/100)+(dedama_per_kakuhen+NB.get(10).first())*0.04);
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
	        double tmp = jack_prob*(1-jack_prob_yutime) + 7.67*jack_prob_yutime;
	        System.out.println("1Rあたりの期待出玉 "+ getDedama_per_jack()/round_num());
	        System.out.println("1回の初当たり時の期待出玉: " + getDedama_per_jack());
	        return tmp / getDedama_per_jack();
	    }

	    double getTouka_border(){
	        return getJack_per_tama() * 250;
	    }
	}