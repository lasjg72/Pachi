package �n���C;

import java.util.Map;


public class Spec_Chichukai {
    /**
    	jack_prob : �哖����m���̕���i1/99�Ȃ�99�j
    	normal_prob : �ʏ킪����������m��
    	kakuhen_prob: �m�ς�����������m��
    	chain_ave : �m�ώ��̕��ϘA�`������
    	round_num_ave : �哖����1�񂠂���̕��σ��E���h��
     **/
	
	double jack_prob = 89.8;
	double normal_prob = 0;
	double kakuhen_prob = 100; 
	double cahin_ave = 1/(1-(1-(through_prob(32.5, 20))*1/(1-through_prob(89.8, 80))*100));
	double round_num_ave = 1*(1-through_prob(32.5, 20))+ (1-through_prob(32.5, 20))* (1/(1-(through_prob(32.5, 20))*(1/(1-through_prob(89.8, 80)));
	
    // �m��jack_prob�̂��̂�num_game��]�n�}��m��
    double through_prob(double jack_prob, double num_game) {
        return Math.pow((jack_prob-1)/jack_prob, num_game);
    }
  //ST20��̌p����
    0.53523420041873641631820920550148
    //ST20��̔�p����
    0.46476579958126358368179079449852
    //ST20��̕��ϘA��
    2.1516213131451630002471889090275


    //���Z80��̌p����
    0.40825388750940472489888576778492
    //ST20��+���Z80��̌p����
    0.78148855695106299978885934920337
    //ST20��+���Z80��̔�p����
    0.21851144304893700021114065079663
    //ST20��+���Z80��̕��ϘA��
    4.5764193675479218012579743351449

}
   
