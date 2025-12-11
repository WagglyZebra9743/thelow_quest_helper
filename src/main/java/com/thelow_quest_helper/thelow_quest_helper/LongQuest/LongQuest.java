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
	//それぞれ定義する
	private static final LongQuest JealousFellow = new LongQuest("冥妬の仲間",4,new JealousFellow().List);
	private static final LongQuest Kerogelos = new LongQuest("The Darkness of Kerogelos(カロゲロスの愛)",30,new Kerogelos().List);
	
	//リストにしておく
	private static final LongQuest[] LongQuestList = {JealousFellow,Kerogelos,};
	
	public static void CheckPhaseByID(final String questid){
		if(questid==null||!thelow_quest_helperConfig.LongQuestEnable)return;
		for(final LongQuest TheLongQuest : LongQuestList) {
			final ThePhase[] ThePhaseList = TheLongQuest.QuestList;
			for(final ThePhase ThePhase : ThePhaseList) {
				if(!ThePhase.QuestID.equals(questid))continue;
				AddPhaseMarker(TheLongQuest,ThePhase);
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
					AddPhaseMarker(TheLongQuest,ThePhase);
				}
				final String Finishtext = ThePhase.FinishPhaseTitle;
				if(questtitle.equals(Finishtext)) {
					RemovePhaseMarker(TheLongQuest,ThePhase);
				}
			}
		}
	}
	
	private static void AddPhaseMarker(final LongQuest TheLongQuest ,final ThePhase ThePhase) {
		final int PhaseIndex = ThePhase.PhaseIndex;
		TheLongQuest.QuestPhase[PhaseIndex] = true;
		final double TheGoalX = (double)ThePhase.PhaseGoal.getX();
		final double TheGoalY = (double)ThePhase.PhaseGoal.getY();
		final double TheGoalZ = (double)ThePhase.PhaseGoal.getZ();
		final String TheMarkerName = "§6[Quest]§f"+TheLongQuest.QuestName+"\n"+ThePhase.GoalText;
		final String TheMarkerID = ThePhase.MarkerID;
		LongQuestMarker.addMarker(TheGoalX, TheGoalY, TheGoalZ, TheMarkerName,TheMarkerID);
		if(ThePhase.HasSubMarker) {
			final double TheSubX = (double)ThePhase.SubPos.getX();
			final double TheSubY = (double)ThePhase.SubPos.getY();
			final double TheSubZ = (double)ThePhase.SubPos.getZ();
			final String TheSubName = ThePhase.SubText;
			final String TheSubID = ThePhase.MarkerID + "-sub";
			LongQuestMarker.addSubMarker(TheSubX, TheSubY, TheSubZ, TheSubName, TheSubID);
		}
	}
	
	public static void ClearPhaseStats() {
		for(LongQuest TheLongQuest : LongQuestList) {
			TheLongQuest.QuestPhase = new boolean[TheLongQuest.QuestPhase.length];
		}
	}
	
	private static void RemovePhaseMarker(final LongQuest TheLongQuest, final ThePhase ThePhase) {
		final int PhaseIndex = ThePhase.PhaseIndex;
		TheLongQuest.QuestPhase[PhaseIndex] = false;
		final String MarkerID = ThePhase.MarkerID;
		LongQuestMarker.RemoveMarkerByID(MarkerID);
	}
	
	public static LongQuest[] GetLongQuestList() {
		return LongQuestList;
	}
}