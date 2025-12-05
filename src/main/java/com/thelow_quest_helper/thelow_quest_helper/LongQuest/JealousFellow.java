package com.thelow_quest_helper.thelow_quest_helper.LongQuest;

import net.minecraft.util.BlockPos;

public class JealousFellow{
	public ThePhase[] List;
	public JealousFellow(){
		ThePhase[] TheList = new ThePhase[4];
		TheList[0] = new ThePhase("§6§l[Quest] クエスト開始§r:§6冥妬の仲間-story-1§r", "§6§l[Quest] クエスト完了§r:§6冥妬の仲間-story-1§r", "§0id:冥妬の仲間-story-1", 0, new BlockPos(421.0, 77.0, 695.0),"§6[Quest]§f冥妬の洞窟：魔女の家","冥妬の洞窟：魔女の家を攻略する","JF_1");
		TheList[1] = new ThePhase("§6§l[Quest] クエスト開始§r:§6冥妬の仲間-story-2§r", "§6§l[Quest] クエスト完了§r:§6冥妬の仲間-story-2§r", "§0id:冥妬の仲間-story-2", 1, new BlockPos(802.0, 82.0, 451.0),"§6[Quest]§f冥妬の洞窟：アラクノヴァリー","冥妬の洞窟：アラクノヴァリーを攻略する","JF_2");
		TheList[2] = new ThePhase("§6§l[Quest] クエスト開始§r:§6冥妬の仲間-story-3§r", "§6§l[Quest] クエスト完了§r:§6冥妬の仲間-story-3§r", "§0id:冥妬の仲間-story-3", 2, new BlockPos(982.0, 75.0, 594.0),"§6[Quest]§f冥妬の洞窟：ジャリエ～陰鬱の底～","冥妬の洞窟：ジャリエ～陰鬱の底～を攻略する","JF_3");
		TheList[3] = new ThePhase("§6§l[Quest] クエスト開始§r:§6冥妬の仲間-story-4§r", "§6§l[Quest] クエスト完了§r:§6冥妬の仲間-story-4§r", "§0id:冥妬の仲間-story-4", 3, new BlockPos(1204.0, 101.0, 172.0),"§6[Quest]§f冥妬の洞窟：鬼哭の遺跡","冥妬の洞窟：鬼哭の遺跡を攻略する","JF_4");
		this.List = TheList;		
	}
}

/*
段階1: {display:{Lore:[0:"§a[INFO]",1:" §f冥妬の洞窟：§9§l魔女の家§rを攻略し、魔女:ドレイクと話をしよう",2:"",3:"§a[クリア条件]",4:" §f冥妬の洞窟：§9§l魔女の家§rを1回攻略する。",5:"§0quest_viewer_item",6:"§a[進行状況]",7:" §f達成度(0/1)",8:"§8破棄する場合はこの本を捨ててください。",9:"§0id:冥妬の仲間-story-1"],Name:"§d冥妬の仲間-story-1"}}
{index:0,title:§6§l[Quest] クエスト開始§r:§6冥妬の仲間-story-1§r,pos:BlockPos{x=371, y=88, z=561}},
{index:1,title:§r§6冥妬の洞窟：§9§l魔女の家§r§r:§r§r,pos:BlockPos{x=2820, y=70, z=8109}},

段階1-2: {display:{Lore:[0:"§a[INFO]",1:" §f冥妬の洞窟：§9§l魔女の家§rを攻略し、魔女:ドレイクと話をしよう",2:"",3:"§a[クリア条件]",4:" §f冥妬の洞窟：§9§l魔女の家§rを1回攻略する。",5:"§0quest_viewer_item",6:"§a[進行状況]",7:" §f達成度(1/1)",8:" §f魔女:ドレイクのところへ報告へ行こう",9:"§8破棄する場合はこの本を捨ててください。",10:"§0id:冥妬の仲間-story-1"],Name:"§d冥妬の仲間-story-1"}}
{index:2,title:§r§6[Quest]§r§a冥妬の仲間-story-1 (1/1)§r,pos:BlockPos{x=2823, y=163, z=8173}},
{index:3,title:§r§6[Quest]§aクエストクリア!!!  §r魔女:ドレイク§rのところへ行って左クリックしよう§r,pos:BlockPos{x=2823, y=163, z=8173}},
{index:4,title:§6§l[Quest] クエスト完了§r:§6冥妬の仲間-story-1§r,pos:BlockPos{x=2822, y=163, z=8170}},

段階2: {display:{Lore:[0:"§a[INFO]",1:" §f冥妬の洞窟：アラクノヴァリーを攻略し、セルフィアと話をしよう",2:"",3:"§a[クリア条件]",4:" §f冥妬の洞窟：アラクノヴァリーを1回攻略する。",5:"§0quest_viewer_item",6:"§a[進行状況]",7:" §f達成度(0/1)",8:"§8破棄する場合はこの本を捨ててください。",9:"§0id:冥妬の仲間-story-2"],Name:"§d冥妬の仲間-story-2"}}
{index:5,title:§6§l[Quest] クエスト開始§r:§6冥妬の仲間-story-2§r,pos:BlockPos{x=2818, y=163, z=8171}},
{index:6,title:§r§6冥妬の洞窟：アラクノヴァリー§r:§r§r,pos:BlockPos{x=5044, y=81, z=1969}},
{index:7,title:§r§6[Quest]§r§a冥妬の仲間-story-2 (1/1)§r,pos:BlockPos{x=5007, y=101, z=1719}},

段階2-2: {display:{Lore:[0:"§a[INFO]",1:" §f冥妬の洞窟：アラクノヴァリーを攻略し、セルフィアと話をしよう",2:"",3:"§a[クリア条件]",4:" §f冥妬の洞窟：アラクノヴァリーを1回攻略する。",5:"§0quest_viewer_item",6:"§a[進行状況]",7:" §f達成度(1/1)",8:" §fアラクノヴァリー住人：セルフィアのところへ報告へ行こう",9:"§8破棄する場合はこの本を捨ててください。",10:"§0id:冥妬の仲間-story-2"],Name:"§d冥妬の仲間-story-2"}}
{index:8,title:§r§6[Quest]§aクエストクリア!!!  §rアラクノヴァリー住人：セルフィア§rのところへ行って左クリックしよう§r,pos:BlockPos{x=5007, y=101, z=1719}},
{index:9,title:§6§l[Quest] クエスト完了§r:§6冥妬の仲間-story-2§r,pos:BlockPos{x=5007, y=101, z=1718}},

段階3: {display:{Lore:[0:"§a[INFO]",1:" §f冥妬の洞窟：ジャリエ～陰鬱の底～を攻略し、冒険者：ルカと話をしよう",2:"",3:"§a[クリア条件]",4:" §f冥妬の洞窟：ジャリエ～陰鬱の底～を1回攻略する。",5:"§0quest_viewer_item",6:"§a[進行状況]",7:" §f達成度(0/1)",8:"§8破棄する場合はこの本を捨ててください。",9:"§0id:冥妬の仲間-story-3"],Name:"§d冥妬の仲間-story-3"}}
{index:10,title:§6§l[Quest] クエスト開始§r:§6冥妬の仲間-story-3§r,pos:BlockPos{x=5006, y=101, z=1715}},
{index:11,title:§r§6冥妬の洞窟：ジャリエ～陰鬱の底～§r:§r§r,pos:BlockPos{x=4294, y=168, z=-3520}},
{index:12,title:§r§6[Quest]§r§a冥妬の仲間-story-3 (1/1)§r,pos:BlockPos{x=4417, y=163, z=-3500}},

段階3-2: {display:{Lore:[0:"§a[INFO]",1:" §f冥妬の洞窟：ジャリエ～陰鬱の底～を攻略し、冒険者：ルカと話をしよう",2:"",3:"§a[クリア条件]",4:" §f冥妬の洞窟：ジャリエ～陰鬱の底～を1回攻略する。",5:"§0quest_viewer_item",6:"§a[進行状況]",7:" §f達成度(1/1)",8:" §f冒険者：ルカのところへ報告へ行こう",9:"§8破棄する場合はこの本を捨ててください。",10:"§0id:冥妬の仲間-story-3"],Name:"§d冥妬の仲間-story-3"}}
{index:13,title:§r§6[Quest]§aクエストクリア!!!  §r冒険者：ルカ§rのところへ行って左クリックしよう§r,pos:BlockPos{x=4417, y=163, z=-3500}},
{index:14,title:§6§l[Quest] クエスト完了§r:§6冥妬の仲間-story-3§r,pos:BlockPos{x=4418, y=163, z=-3500}},

段階4: {display:{Lore:[0:"§a[INFO]",1:" §f冥妬の洞窟：鬼哭の遺跡を攻略し、サルダナと話をしよう",2:"",3:"§a[クリア条件]",4:" §f冥妬の洞窟：§c§l鬼哭の遺跡§rを1回攻略する。",5:"§0quest_viewer_item",6:"§a[進行状況]",7:" §f達成度(0/1)",8:"§8破棄する場合はこの本を捨ててください。",9:"§0id:冥妬の仲間-story-4"],Name:"§d冥妬の仲間-story-4"}}
{index:15,title:§6§l[Quest] クエスト開始§r:§6冥妬の仲間-story-4§r,pos:BlockPos{x=4422, y=163, z=-3500}},
{index:16,title:§r§6冥妬の洞窟：§c§l鬼哭の遺跡§r§r:§r§r,pos:BlockPos{x=2877, y=12, z=154}},
{index:17,title:§r§6[Quest]§r§a冥妬の仲間-story-4 (1/1)§r,pos:BlockPos{x=3319, y=39, z=-82}},

段階4-2: {display:{Lore:[0:"§a[INFO]",1:" §f冥妬の洞窟：鬼哭の遺跡を攻略し、サルダナと話をしよう",2:"",3:"§a[クリア条件]",4:" §f冥妬の洞窟：§c§l鬼哭の遺跡§rを1回攻略する。",5:"§0quest_viewer_item",6:"§a[進行状況]",7:" §f達成度(1/1)",8:" §fサルダナのところへ報告へ行こう",9:"§8破棄する場合はこの本を捨ててください。",10:"§0id:冥妬の仲間-story-4"],Name:"§d冥妬の仲間-story-4"}}
{index:18,title:§r§6[Quest]§aクエストクリア!!!  §rサルダナ§rのところへ行って左クリックしよう§r,pos:BlockPos{x=3319, y=39, z=-82}},
{index:19,title:§6§l[Quest] クエスト完了§r:§6冥妬の仲間-story-4§r,pos:BlockPos{x=3321, y=39, z=-97}}
*/