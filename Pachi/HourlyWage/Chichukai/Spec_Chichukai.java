package 地中海;

import java.util.Map;


public class Spec_Chichukai {
    /**
    	jack_prob : 大当たり確率の分母（1/99なら99）
    	normal_prob : 通常が当たる実質確率
    	kakuhen_prob: 確変が当たる実質確率
    	chain_ave : 確変時の平均連チャン数
    	round_num_ave : 大当たり1回あたりの平均ラウンド数
     **/
	
	double jack_prob = 89.8;
	double normal_prob = 0;
	double kakuhen_prob = 100; 
	double cahin_ave = 1/(1-(1-(through_prob(32.5, 20))*1/(1-through_prob(89.8, 80))*100));
	double round_num_ave = 1*(1-through_prob(32.5, 20))+ (1-through_prob(32.5, 20))* (1/(1-(through_prob(32.5, 20))*(1/(1-through_prob(89.8, 80)));
	
    // 確率jack_probのものがnum_game回転ハマる確率
    double through_prob(double jack_prob, double num_game) {
        return Math.pow((jack_prob-1)/jack_prob, num_game);
    }
  //ST20回の継続率
    0.53523420041873641631820920550148
    //ST20回の非継続率
    0.46476579958126358368179079449852
    //ST20回の平均連数
    2.1516213131451630002471889090275


    //時短80回の継続率
    0.40825388750940472489888576778492
    //ST20回+時短80回の継続率
    0.78148855695106299978885934920337
    //ST20回+時短80回の非継続率
    0.21851144304893700021114065079663
    //ST20回+時短80回の平均連数
    4.5764193675479218012579743351449

}
   
