package com.thelow_quest_helper.thelow_quest_helper.item;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class time_creater {

	public static String operation(int timemin) {
		if(timemin==0) return "まもなく";
		//今の時刻に対してtimemin分加算したときの時間をStringとして取得
		 String FutureTime = LocalDateTime.now().plus(Duration.ofMinutes(timemin)).format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm"));
		
		 //一日は1440分
		 int timeday = timemin / 1440;
		 //その余りは一日に満たさなかった部分
		 timemin %= 1440;
		 //一時間は60分
		 int timehour = timemin / 60;
		 //その余りは一時間にも満たなかった部分
		 timemin %= 60;
		 
		 //テキストを初期化
		 String text = null;
		 
		 if(timeday>0) {//一日以上なら
			text=timeday+"日";
			text=text+timehour+"時間";
			text=text+timemin+"分後,予想時刻:";
		 }else if(timehour>0) {//一時間以上なら
			 text=timehour+"時間";
			 text=text+timemin+"分後,予想時刻:";
		 }else if(timemin>0) {//一分以上なら
			 text=timemin+"分後,予想時刻:";
		 }
		 
		 if(text==null||text.isEmpty())return null;
		 
	     text = text + FutureTime;
	     return text;
	     
		
	}
}