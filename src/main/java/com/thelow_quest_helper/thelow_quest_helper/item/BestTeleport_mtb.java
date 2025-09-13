package com.thelow_quest_helper.thelow_quest_helper.item;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;


public class BestTeleport_mtb {

		
	// Matatabi から移植した最良テレポート手段を返すクラス
    private static class TeleportPoint {
        String name;
        int x, y, z;
        String groupName;
        
        //コンストラクターの定義
        TeleportPoint(String name, int x, int y, int z, String groupName) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.groupName = groupName;
        }

        //x,y,zを座標系式(x,y,z)に変換する
        String getCoordinatesString() {
            return String.format("(%d,%d,%d)", x, y, z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TeleportPoint that = (TeleportPoint) o;
            return x == that.x && y == that.y && z == that.z && Objects.equals(name, that.name) && Objects.equals(groupName, that.groupName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, x, y, z, groupName);
        }

        @Override
        public String toString() { // For debugging
            return "TP{" + name + " @ " + getCoordinatesString() + " (" + (groupName == null ? "N/A" : groupName) + ")}";
        }
    }

    // テレポート地点のデータ (変更なし)
    private static final Map<String, List<TeleportPoint>> TELEPORT_GROUPS = new LinkedHashMap<>();
    private static final List<TeleportPoint> ALL_TELEPORT_POINTS = new ArrayList<>();

    // ★2種類のクランテレポートポイントの座標を定義
    //クランハウスの情報
    private static final int CLAN_TP_1_X = -188;
    private static final int CLAN_TP_1_Y = 59;
    private static final int CLAN_TP_1_Z = -1159;
    private static final String CLAN_TP_1_NAME = "clantp";

    //ガチャ広場経由で到着するハルシオンの情報
    private static final int CLAN_TP_2_X = -1253;
    private static final int CLAN_TP_2_Y = 84;
    private static final int CLAN_TP_2_Z = -973;
    private static final String CLAN_TP_2_NAME = "clan+ガチャ広場tp";


    private static final String START_NODE_NAME_PREFIX = "___START_NODE___";
    private static final String END_NODE_NAME_PREFIX = "___END_NODE___";
    private static final String WALK_METHOD = "WALK";


    static {
        // テレポート地点データ (変更なし)
        String groupUmasha = "馬車";
        addTeleportPoint(groupUmasha, "エル・ドール", 38, 119, -71);
        addTeleportPoint(groupUmasha, "タリバンズ", 371, 92, 494);
        addTeleportPoint(groupUmasha, "シュレリッツ", -602, 84, 514);
        addTeleportPoint(groupUmasha, "リグヘルムの里", 343, 76, -1151);
        addTeleportPoint(groupUmasha, "ラジャスタン", -1217, 71, -1317);
        addTeleportPoint(groupUmasha, "オブシー", -1231, 66, 238);
        addTeleportPoint(groupUmasha, "メルトリア王国 (馬車)", -172, 68, -948);

        String groupFuneA = "船A";
        addTeleportPoint(groupFuneA, "フェルトン(船A)", 1147, 68, 714);
        addTeleportPoint(groupFuneA, "スノーリ村", 1151, 68, -775);
        addTeleportPoint(groupFuneA, "アルノース", 1398, 67, -15);

        String groupFuneB = "船B";
        addTeleportPoint(groupFuneB, "フェルトン(船B)", 1075, 68, 769);
        addTeleportPoint(groupFuneB, "ヴェネミア", -127, 69, 897);
        addTeleportPoint(groupFuneB, "ベルフォート", -968, 68, 815);
        addTeleportPoint(groupFuneB, "ハンプニー(船B)", -891, 68, 1163);

        String groupHikuutei = "飛空艇";
        addTeleportPoint(groupHikuutei, "ビルニス", -150, 185, 1291);
        addTeleportPoint(groupHikuutei, "メリアル城塞", 1319, 127, -929);
        addTeleportPoint(groupHikuutei, "ハンプニー(飛空艇)", -938, 103, 1143);
        addTeleportPoint(groupHikuutei, "ハルシオン(飛空艇)", -1131, 129, -792);
        addTeleportPoint(groupHikuutei, "ヴェネミア(飛空艇)", -37, 129, 910);
        addTeleportPoint(groupHikuutei, "メルトリア王国 (飛空艇)", -384, 102, -1263);
        addTeleportPoint(groupHikuutei, "アシュビ修道院", -463, 152, -608);
    }

    //その座標を保存する
    private static void addTeleportPoint(String groupName, String pointName, int x, int y, int z) {
        TeleportPoint tp = new TeleportPoint(pointName, x, y, z, groupName);
        TELEPORT_GROUPS.computeIfAbsent(groupName, k -> new ArrayList<>()).add(tp);
        ALL_TELEPORT_POINTS.add(tp);
    }

    //三次元座標間の距離を測定
    private double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
    }

    // PredecessorInfo class (変更なし)
    private static class PredecessorInfo {
        TeleportPoint previousPoint;
        String travelMethod;
        double distanceSegment;

        PredecessorInfo(TeleportPoint previousPoint, String travelMethod, double distanceSegment) {
            this.previousPoint = previousPoint;
            this.travelMethod = travelMethod;
            this.distanceSegment = distanceSegment;
        }
    }

    private static class RouteResult {
        double totalDistance;
        String nextDistCoordStr;
        String nextGoalCoordStr;
        String nextDistNameStr;
        String pathDescription;
        LinkedList<TeleportPoint> fullPath; //これに移動経路データを仮保存する

        RouteResult(double totalDistance, String nextDistCoordStr, String nextGoalCoordStr, String nextDistNameStr, String pathDescription, LinkedList<TeleportPoint> fullPath) {
            this.totalDistance = totalDistance;
            this.nextDistCoordStr = nextDistCoordStr;
            this.nextGoalCoordStr = nextGoalCoordStr;
            this.nextDistNameStr = nextDistNameStr;
            this.pathDescription = pathDescription;
            this.fullPath = fullPath; //これに移動経路データを仮保存する
        }

        static RouteResult noPath(String startCoordName, String endNodeCoordStr, double directDist) {
            String pathDesc;
            String nextDistNameIfNoTeleport = "目的地";
            if (Double.isInfinite(directDist) || Double.isNaN(directDist)) {
                 pathDesc = startCoordName + " -> 目的地 (到達可能な経路が見つかりません)";
                 // ★ 経路が無いので空のパスリストを渡す
                 return new RouteResult(Double.POSITIVE_INFINITY, endNodeCoordStr, endNodeCoordStr, nextDistNameIfNoTeleport, pathDesc, new LinkedList<>());
            }
            pathDesc = String.format("%s §e->§7(移動%d m) §e->目的地", startCoordName, Math.round(directDist));
            // ★ 多分直接移動だから空のパスリストを渡す
            return new RouteResult(directDist, endNodeCoordStr, endNodeCoordStr, nextDistNameIfNoTeleport, pathDesc, new LinkedList<>());
        }
    }

    //特に大きく変更はしていない
    //移動経路にカラーコードを追加した程度
private RouteResult findShortestPathUsingDijkstra(int startX, int startY, int startZ, int endX, int endY, int endZ, String goalName) {
     TeleportPoint startNode = new TeleportPoint(START_NODE_NAME_PREFIX + UUID.randomUUID(), startX, startY, startZ, null);
     TeleportPoint endNode = new TeleportPoint(END_NODE_NAME_PREFIX + UUID.randomUUID(), endX, endY, endZ, null);
     String endNodeCoordStr = endNode.getCoordinatesString();

     List<TeleportPoint> graphNodes = new ArrayList<>(ALL_TELEPORT_POINTS);
     graphNodes.add(startNode);
     graphNodes.add(endNode);

     Map<TeleportPoint, Double> distances = new HashMap<>();
     Map<TeleportPoint, PredecessorInfo> predecessors = new HashMap<>();
     PriorityQueue<Map.Entry<TeleportPoint, Double>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());
     
     for (TeleportPoint node : graphNodes) {
         distances.put(node, Double.POSITIVE_INFINITY);
     }
     distances.put(startNode, 0.0);
     pq.add(new AbstractMap.SimpleEntry<>(startNode, 0.0));
     Set<TeleportPoint> settledNodes = new HashSet<>();

     while (!pq.isEmpty()) {
         TeleportPoint u = pq.poll().getKey();
         if (settledNodes.contains(u)) continue;
         settledNodes.add(u);
         if (u.equals(endNode)) break;

         for (TeleportPoint v_walk_target : graphNodes) {
             if (v_walk_target.equals(u)) continue;
             double walkDistance = calculateDistance(u.x, u.y, u.z, v_walk_target.x, v_walk_target.y, v_walk_target.z);
             if (distances.get(u) + walkDistance < distances.get(v_walk_target)) {
                 distances.put(v_walk_target, distances.get(u) + walkDistance);
                 predecessors.put(v_walk_target, new PredecessorInfo(u, WALK_METHOD, walkDistance));
                 pq.add(new AbstractMap.SimpleEntry<>(v_walk_target, distances.get(v_walk_target)));
             }
         }
         if (u.groupName != null && TELEPORT_GROUPS.containsKey(u.groupName)) {
             for (TeleportPoint v_teleport_target : TELEPORT_GROUPS.get(u.groupName)) {
                 if (v_teleport_target.equals(u)) continue;
                 double teleportCost = 0.0;
                 if (distances.get(u) + teleportCost < distances.get(v_teleport_target)) {
                     distances.put(v_teleport_target, distances.get(u) + teleportCost);
                     predecessors.put(v_teleport_target, new PredecessorInfo(u, u.groupName, teleportCost));
                     pq.add(new AbstractMap.SimpleEntry<>(v_teleport_target, distances.get(v_teleport_target)));
                 }
             }
         }
     }
     
     double finalDistanceToEnd = distances.get(endNode);
     String startNodeDisplayNameForNoPath = "出発地"; 
     if (finalDistanceToEnd == Double.POSITIVE_INFINITY) {
         double directDist = calculateDistance(startX, startY, startZ, endX, endY, endZ);
         // noPath の戻り値も goalName を使うように変更
         RouteResult noPathResult = RouteResult.noPath(startNodeDisplayNameForNoPath, endNodeCoordStr, directDist);
         noPathResult.pathDescription = noPathResult.pathDescription.replace("目的地", goalName);//目的地がNPCの名前やダンジョン名になるようにしている
         return noPathResult;
     }

     LinkedList<TeleportPoint> path = new LinkedList<>();
     TeleportPoint current = endNode;
     while (current != null && !current.equals(startNode) && predecessors.containsKey(current)) {
         path.addFirst(current);
         PredecessorInfo predInfo = predecessors.get(current);
         if (predInfo == null) break;
         current = predInfo.previousPoint;
     }
     if (!startNode.equals(endNode)) path.addFirst(startNode);
     else if (path.isEmpty() && startNode.equals(endNode)) path.addFirst(startNode);

     StringBuilder pathDescBuilder = new StringBuilder();
     String nextDist = endNodeCoordStr;
     String nextGoal = endNodeCoordStr;
     String nextDistName = goalName; //目的地をNPC名やダンジョン名にしている
     boolean firstTeleportFound = false;

     if (startNode.equals(endNode) && finalDistanceToEnd == 0) {
         pathDescBuilder.append("出発地と目的地が同じです");
     } else if (path.size() <= 1 && !startNode.equals(endNode)){
          long blocks = Math.round(finalDistanceToEnd);
          pathDescBuilder.append(String.format(" §e->§7(移動%d m) §e->§f%s", blocks, goalName));
     } else {
         // ★★★ 文字列フォーマットのロジックをここから変更 ★★★byAI
         String initialPointName = path.get(0).name.startsWith(START_NODE_NAME_PREFIX) ? "出発地" : path.get(0).name;
         pathDescBuilder.append(initialPointName); // 開始地点名だけを先に追加

         for (int i = 0; i < path.size() - 1; i++) {
             TeleportPoint p1_segment_start = path.get(i);
             TeleportPoint p2_segment_end = path.get(i + 1);
             PredecessorInfo travelInfo = predecessors.get(p2_segment_end);

             if (travelInfo == null) {
                  pathDescBuilder.append(" -> ??? -> ");
                  pathDescBuilder.append(p2_segment_end.name.startsWith(END_NODE_NAME_PREFIX) ? goalName : p2_segment_end.name);
                  continue;
             }
             
             String p2DisplayName = p2_segment_end.name.startsWith(END_NODE_NAME_PREFIX) ? goalName : p2_segment_end.name;

             if (travelInfo.travelMethod.equals(WALK_METHOD)) {
                 long blocks = Math.round(travelInfo.distanceSegment);
                 pathDescBuilder.append(" §e->§7(移動").append(blocks).append(" m)");
             } else { 
                 if (!firstTeleportFound) {
                     nextDist = p1_segment_start.getCoordinatesString();
                     nextGoal = p2_segment_end.getCoordinatesString();
                     nextDistName = p2_segment_end.name.startsWith(END_NODE_NAME_PREFIX) ? goalName : p2_segment_end.name;
                     firstTeleportFound = true;
                 }
                 pathDescBuilder.append(" §e->§b(").append(travelInfo.travelMethod).append(")");
             }
             
             // 目的地かどうかに応じて色を切り替える
             if (p2DisplayName.equals(goalName)) {
                 pathDescBuilder.append(" §e->§f").append(p2DisplayName);
             } else {
                 pathDescBuilder.append(" §e->§a").append(p2DisplayName);
             }
         }
     }
     
     String finalPathString = pathDescBuilder.toString();
     if (finalPathString.isEmpty() && finalDistanceToEnd != Double.POSITIVE_INFINITY && !startNode.equals(endNode)) {
          long blocks = Math.round(finalDistanceToEnd);
          finalPathString = String.format(" §e->§7(移動%d m) §e->§f%s", blocks, goalName);
     } else if (finalPathString.isEmpty() && startNode.equals(endNode)) {
         finalPathString = "出発地と目的地が同じです";
     }

     return new RouteResult(finalDistanceToEnd, nextDist, nextGoal, nextDistName, finalPathString, path); // ★ path を追加
 }
 
//経由地にも名前を付けたマーカーを設置しておくとメッセージ表示に役立つ
 public static String findNameByPos(int x, int y, int z) {
     TeleportPoint closestPoint = null;
     double minDistance = Double.MAX_VALUE;

     // 全てのテレポート地点をチェック
     for (TeleportPoint point : ALL_TELEPORT_POINTS) {
         double distance = Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2) + Math.pow(point.z - z, 2));
         if (distance < minDistance) {
             minDistance = distance;
             closestPoint = point;
         }
     }

     // 距離が非常に近い場合（ここでは2ブロック以内）に限り、その地名を採用する
     if (closestPoint != null && minDistance < 2.0) {
         return closestPoint.name;
     }

     // 見つからなかった場合はデフォルトの名前を返す
     return "経由地";
 }

    /**
     * 指定された2つの座標間で、最短となる移動ルートを判断し、その情報を文字列配列で返します。
     * isClanTP が true の場合、プレイヤー開始位置、クランTP1、クランTP2からの開始を比較します。
     */
 
 //大きな変更点なし
 public String[] getBestRoute(int x1, int y1, int z1, int x2, int y2, int z2, boolean isClanTP) {
	    // 最初にマーカーをクリア
	    MarkerRenderer.clearMarkers();

	    RouteResult routeFromP1 = findShortestPathUsingDijkstra(x1, y1, z1, x2, y2, z2, Keyclick.goalname);
	    RouteResult bestRoute = routeFromP1;
	    String startPointString = "";

	    if (isClanTP) {
	        RouteResult routeFromClanTp1 = findShortestPathUsingDijkstra(CLAN_TP_1_X, CLAN_TP_1_Y, CLAN_TP_1_Z, x2, y2, z2, Keyclick.goalname);
	        RouteResult routeFromClanTp2 = findShortestPathUsingDijkstra(CLAN_TP_2_X, CLAN_TP_2_Y, CLAN_TP_2_Z, x2, y2, z2, Keyclick.goalname);
	        
	        if (routeFromClanTp1.totalDistance < bestRoute.totalDistance) {
	            bestRoute = routeFromClanTp1;
	            startPointString = " §e->§b[" + CLAN_TP_1_NAME + "] §e->§aクランハウス";
	        }
	        if (routeFromClanTp2.totalDistance < bestRoute.totalDistance) {
	            bestRoute = routeFromClanTp2;
	            startPointString = " §e->§b[" + CLAN_TP_2_NAME + "] §e->§aハルシオン(ミニゲーム案内)";
	        }
	    }

	    // ★★★ 最適なルートが決まった後、その経由地リストを使ってマーカーを登録 ★★★
	    if (bestRoute.fullPath != null && !bestRoute.fullPath.isEmpty()) {
	        for (TeleportPoint point : bestRoute.fullPath) {
	        	
	            // 出発地点に応じてスタートのマーカーを置く
	            if (point.name.startsWith(START_NODE_NAME_PREFIX)) {
	            	if(startPointString.contains("クランハウス")) {
	            		//クランハウスにもマーカーを置くことで、次の移動先案内が出るようにする
		            	MarkerRenderer.addMarker(-188.0, 59.0, -1159.0, "クランハウス");
		            }else if(startPointString.contains("ミニゲーム案内")) {
		            	//到達地はハルシオンだがクランハウスとガチャ広場を経由する
		            	MarkerRenderer.addMarker(-1253.0, 84.0, -973.0, "ハルシオン");
		            }
	                continue;
	            }
	            
	            String markerName = point.name.startsWith(END_NODE_NAME_PREFIX) ? Keyclick.goalname : point.name;
	            //マーカーに登録する部分
	            MarkerRenderer.addMarker((double)point.x, (double)point.y, (double)point.z, markerName);
	        }
	    } else {
	        // テレポート地点を経由しない単純な徒歩ルートの場合
	        MarkerRenderer.addMarker((double)x2, (double)y2, (double)z2, Keyclick.goalname);
	    }

	    //出発地を最初の通過地点に変更する
	    String finalPath = bestRoute.pathDescription.replaceFirst("出発地", startPointString);
	    String totalDistanceStr = String.format("%.0f m", bestRoute.totalDistance);

	    // 戻り値の形式は変えずに、元の情報を返す
	    return new String[]{
	        bestRoute.nextDistCoordStr,
	        bestRoute.nextGoalCoordStr,
	        bestRoute.nextDistNameStr,
	        finalPath,
	        totalDistanceStr
	    };
	}
}
