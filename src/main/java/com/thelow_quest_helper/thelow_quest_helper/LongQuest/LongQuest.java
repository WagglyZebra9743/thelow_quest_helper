package com.thelow_quest_helper.thelow_quest_helper.LongQuest;

import com.thelow_quest_helper.thelow_quest_helper.config.thelow_quest_helperConfig;

public class LongQuest{
	final String QuestName;
	public boolean[] QuestPhase;
	final ThePhase[] QuestList;
	LongQuest(final String theQuestName,final int theQuestLength,final ThePhase[] QuestList){
		this.QuestName = theQuestName;
		boolean[] theQuestPhase = new boolean[theQuestLength];
		this.QuestPhase = theQuestPhase;
		this.QuestList = QuestList;
	}
	public static final int LongQuestCount = 1;
	//それぞれ定義する
	private static final LongQuest JealousFellow = new LongQuest("冥妬の仲間",4,new JealousFellow().List);
	
	//リストにしておく
	private static final LongQuest[] LongQuestList = {JealousFellow,};
	
	public static void CheckPhaseByID(final String questid){
		if(questid==null||!thelow_quest_helperConfig.LongQuestEnable)return;
		for(final LongQuest TheLongQuest : LongQuestList) {
			final ThePhase[] ThePhaseList = TheLongQuest.QuestList;
			for(final ThePhase ThePhase : ThePhaseList) {
				if(!ThePhase.QuestID.equals(questid))continue;
				AddGoalMarker(TheLongQuest,ThePhase);
			}
		}
	}
	
	public static void UpdatePhaseByTitle(final String questtitle) {
		if(questtitle==null||!thelow_quest_helperConfig.LongQuestEnable)return;
		for(final LongQuest TheLongQuest : LongQuestList) {
			final ThePhase[] ThePhaseList = TheLongQuest.QuestList;
			for(final ThePhase ThePhase : ThePhaseList) {
				final String Starttext = ThePhase.StartPhaseTitle;
				if(questtitle.equals(Starttext)) {
					AddGoalMarker(TheLongQuest,ThePhase);
				}
				final String Finishtext = ThePhase.FinishPhaseTitle;
				if(questtitle.equals(Finishtext)) {
					RemoveGoalMarker(TheLongQuest,ThePhase);
				}
			}
		}
	}
	
	private static void AddGoalMarker(final LongQuest TheLongQuest ,final ThePhase ThePhase) {
		final int PhaseIndex = ThePhase.PhaseIndex;
		TheLongQuest.QuestPhase[PhaseIndex] = true;
		final double TheGoalX = (double)ThePhase.PhaseGoal.getX();
		final double TheGoalY = (double)ThePhase.PhaseGoal.getY();
		final double TheGoalZ = (double)ThePhase.PhaseGoal.getZ();
		final String TheMarkerName = ThePhase.GoalText;
		final String TheMarkerID = ThePhase.MarkerID;
		LongQuestMarker.addMarker(TheGoalX, TheGoalY, TheGoalZ, TheMarkerName,TheMarkerID);
	}
	
	private static void RemoveGoalMarker(final LongQuest TheLongQuest, final ThePhase ThePhase) {
		final int PhaseIndex = ThePhase.PhaseIndex;
		TheLongQuest.QuestPhase[PhaseIndex] = false;
		final String MarkerID = ThePhase.MarkerID;
		LongQuestMarker.RemoveMarkerByID(MarkerID);
	}
	
	public static LongQuest GetJealousFellowStats(){
		return JealousFellow;
	}
}