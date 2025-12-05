package com.thelow_quest_helper.thelow_quest_helper.LongQuest;

import net.minecraft.util.BlockPos;

public class ThePhase {
	public final String StartPhaseTitle;
	public final String FinishPhaseTitle;
	public final String QuestID;
	public final BlockPos PhaseGoal;
	public final String GoalText;
	public final String Description;
	public final String MarkerID;
	public final int PhaseIndex;
	public ThePhase(final String StartTitle,final String FinishTitle,final String PhaseID,final int Index,final BlockPos PhaseGoalPos,final String GoalName,final String Description,final String MarkerID){
		this.StartPhaseTitle = StartTitle;
		this.FinishPhaseTitle = FinishTitle;
		this.QuestID = PhaseID;
		this.PhaseGoal = PhaseGoalPos;
		this.GoalText = GoalName;
		this.Description= Description;
		this.MarkerID = MarkerID;
		this.PhaseIndex = Index;
	}
}
