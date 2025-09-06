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

        TeleportPoint(String name, int x, int y, int z, String groupName) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.groupName = groupName;
        }

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
    private static final int CLAN_TP_1_X = -188;
    private static final int CLAN_TP_1_Y = 59;
    private static final int CLAN_TP_1_Z = -1159;
    private static final String CLAN_TP_1_NAME = "clantp"; // ログや説明用

    private static final int CLAN_TP_2_X = -1253; // 例として異なる座標
    private static final int CLAN_TP_2_Y = 84;
    private static final int CLAN_TP_2_Z = -973;
    private static final String CLAN_TP_2_NAME = "clan + ガチャ広場tp"; // ログや説明用


    private static final String START_NODE_NAME_PREFIX = "___START_NODE___";
    private static final String END_NODE_NAME_PREFIX = "___END_NODE___";
    private static final String WALK_METHOD = "WALK";


    static {
        // テレポート地点データの初期化 (変更なし)
        String groupUmasha = "馬車";
        addTeleportPoint(groupUmasha, "エル・ドール", 38, 119, -71);
        addTeleportPoint(groupUmasha, "タリバンズ", 371, 92, 494);
        addTeleportPoint(groupUmasha, "シュレリッツ", -602, 84, 514);
        addTeleportPoint(groupUmasha, "リグヘルムの里", 343, 76, -1151);
        addTeleportPoint(groupUmasha, "ラジャスタン", -1217, 71, -1317);
        addTeleportPoint(groupUmasha, "オブシー", -1231, 66, 238);
        addTeleportPoint(groupUmasha, "メルトリア王国 (馬車)", -172, 68, -948);

        String groupFuneA = "船A";
        addTeleportPoint(groupFuneA, "フェルトン", 1147, 68, 714);
        addTeleportPoint(groupFuneA, "スノーリ村", 1151, 68, -775);
        addTeleportPoint(groupFuneA, "アルノース", 1398, 67, -15);

        String groupFuneB = "船B";
        addTeleportPoint(groupFuneB, "フェルトン", 1075, 68, 769);
        addTeleportPoint(groupFuneB, "ヴェネミア", -127, 69, 897);
        addTeleportPoint(groupFuneB, "ベルフォート", -968, 68, 815);
        addTeleportPoint(groupFuneB, "パンプニー", -891, 68, 1163);

        String groupHikuutei = "飛空艇";
        addTeleportPoint(groupHikuutei, "ビルニス", -150, 185, 1291);
        addTeleportPoint(groupHikuutei, "メリアル城塞", 1319, 127, -929);
        addTeleportPoint(groupHikuutei, "パンプニー", -938, 103, 1143);
        addTeleportPoint(groupHikuutei, "ハルシオン", -1131, 129, -792);
        addTeleportPoint(groupHikuutei, "ヴェネミア", -37, 129, 910);
        addTeleportPoint(groupHikuutei, "メルトリア王国 (飛空艇)", -384, 102, -1263);
        addTeleportPoint(groupHikuutei, "アシュビ修道院", -463, 152, -608);
    }

    private static void addTeleportPoint(String groupName, String pointName, int x, int y, int z) {
        TeleportPoint tp = new TeleportPoint(pointName, x, y, z, groupName);
        TELEPORT_GROUPS.computeIfAbsent(groupName, k -> new ArrayList<>()).add(tp);
        ALL_TELEPORT_POINTS.add(tp);
    }

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

    // RouteResult class (変更なし)
    private static class RouteResult {
        double totalDistance;
        String nextDistCoordStr;
        String nextGoalCoordStr;
        String nextDistNameStr;
        String pathDescription;

        RouteResult(double totalDistance, String nextDistCoordStr, String nextGoalCoordStr, String nextDistNameStr, String pathDescription) {
            this.totalDistance = totalDistance;
            this.nextDistCoordStr = nextDistCoordStr;
            this.nextGoalCoordStr = nextGoalCoordStr;
            this.nextDistNameStr = nextDistNameStr;
            this.pathDescription = pathDescription;
        }

        static RouteResult noPath(String startCoordName, String endNodeCoordStr, double directDist) {
            String pathDesc;
            String nextDistNameIfNoTeleport = "目的地";
            if (Double.isInfinite(directDist) || Double.isNaN(directDist)) {
                 pathDesc = startCoordName + " -> 目的地 (到達可能な経路が見つかりません)";
                 return new RouteResult(Double.POSITIVE_INFINITY, endNodeCoordStr, endNodeCoordStr, nextDistNameIfNoTeleport, pathDesc);
            }
            pathDesc = String.format("%s -> (%d Blocks) -> 目的地", startCoordName, Math.round(directDist));
            return new RouteResult(directDist, endNodeCoordStr, endNodeCoordStr, nextDistNameIfNoTeleport, pathDesc);
        }
    }

    // findShortestPathUsingDijkstra メソッド (変更なし)
    private RouteResult findShortestPathUsingDijkstra(int startX, int startY, int startZ, int endX, int endY, int endZ) {
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
        String startNodeDisplayNameForNoPath = startNode.name.startsWith(START_NODE_NAME_PREFIX) ? "出発地" : startNode.name; // startNode.name を使う
        if (finalDistanceToEnd == Double.POSITIVE_INFINITY) {
            double directDist = calculateDistance(startX, startY, startZ, endX, endY, endZ);
            return RouteResult.noPath(startNodeDisplayNameForNoPath, endNodeCoordStr, directDist);
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
        String nextDistName = "目的地";
        boolean firstTeleportFound = false;

        if (startNode.equals(endNode) && finalDistanceToEnd == 0) {
            pathDescBuilder.append("出発地と目的地が同じです");
        } else if (path.size() <= 1 && !startNode.equals(endNode)){
             long blocks = Math.round(finalDistanceToEnd);
             String startName = startNode.name.startsWith(START_NODE_NAME_PREFIX) ? "出発地" : startNode.name; // startNode.name を使う
             pathDescBuilder.append(String.format("%s -> (%d Blocks) -> 目的地", startName, blocks));
        } else {
            String initialPointName = path.get(0).name.startsWith(START_NODE_NAME_PREFIX) ? "出発地" : path.get(0).name; // startNode.name を使う
            pathDescBuilder.append(initialPointName);

            for (int i = 0; i < path.size() - 1; i++) {
                TeleportPoint p1_segment_start = path.get(i);
                TeleportPoint p2_segment_end = path.get(i + 1);
                PredecessorInfo travelInfo = predecessors.get(p2_segment_end);

                if (travelInfo == null) {
                     pathDescBuilder.append(" -> ??? -> ");
                     pathDescBuilder.append(p2_segment_end.name.startsWith(END_NODE_NAME_PREFIX) ? "目的地" : p2_segment_end.name);
                     continue;
                }
                
                String p2DisplayName = p2_segment_end.name.startsWith(END_NODE_NAME_PREFIX) ? "目的地" : p2_segment_end.name;

                if (travelInfo.travelMethod.equals(WALK_METHOD)) {
                    long blocks = Math.round(travelInfo.distanceSegment);
                    pathDescBuilder.append(" -> (").append(blocks).append(" Blocks) -> ");
                    pathDescBuilder.append(p2DisplayName);
                } else { 
                    if (!firstTeleportFound) {
                        nextDist = p1_segment_start.getCoordinatesString();
                        nextGoal = p2_segment_end.getCoordinatesString();
                        if (p2_segment_end.name.startsWith(END_NODE_NAME_PREFIX)) {
                            nextDistName = "目的地";
                        } else {
                            nextDistName = p2_segment_end.name;
                        }
                        firstTeleportFound = true;
                    }
                    pathDescBuilder.append(" ~(").append(travelInfo.travelMethod).append(")~ ");
                    pathDescBuilder.append(p2DisplayName);
                }
            }
        }
        
        String finalPathString = pathDescBuilder.toString().replaceAll("\\s*->\\s*->\\s*", " -> ").trim();
        if (finalPathString.isEmpty() && finalDistanceToEnd != Double.POSITIVE_INFINITY && !startNode.equals(endNode)) {
             long blocks = Math.round(finalDistanceToEnd);
             String startName = startNode.name.startsWith(START_NODE_NAME_PREFIX) ? "出発地" : startNode.name; // startNode.name を使う
             finalPathString = String.format("%s -> (%d Blocks) -> 目的地", startName, blocks);
        } else if (finalPathString.isEmpty() && startNode.equals(endNode)) {
            finalPathString = "出発地と目的地が同じです";
        }

        return new RouteResult(finalDistanceToEnd, nextDist, nextGoal, nextDistName, finalPathString);
    }

    /**
     * 指定された2つの座標間で、最短となる移動ルートを判断し、その情報を文字列配列で返します。
     * isClanTP が true の場合、プレイヤー開始位置、クランTP1、クランTP2からの開始を比較します。
     */
    public String[] getBestRoute(int x1, int y1, int z1, int x2, int y2, int z2, boolean isClanTP) {
        RouteResult routeFromP1 = findShortestPathUsingDijkstra(x1, y1, z1, x2, y2, z2);

        if (isClanTP) {
            RouteResult routeFromClanTp1 = findShortestPathUsingDijkstra(CLAN_TP_1_X, CLAN_TP_1_Y, CLAN_TP_1_Z, x2, y2, z2);
            RouteResult routeFromClanTp2 = findShortestPathUsingDijkstra(CLAN_TP_2_X, CLAN_TP_2_Y, CLAN_TP_2_Z, x2, y2, z2);

            RouteResult bestRoute = routeFromP1;
            String prefix = "";

            if (routeFromClanTp1.totalDistance < bestRoute.totalDistance) {
                bestRoute = routeFromClanTp1;
                prefix = "[" + CLAN_TP_1_NAME + "] -> ";
            }
            if (routeFromClanTp2.totalDistance < bestRoute.totalDistance) {
                bestRoute = routeFromClanTp2; // これが最短であれば、上記のprefixは上書きされる
                prefix = "[" + CLAN_TP_2_NAME + "] -> ";
            }
            
            String totalDistanceStr = String.format("%.0f Blocks", bestRoute.totalDistance);
            return new String[]{
                bestRoute.nextDistCoordStr,
                bestRoute.nextGoalCoordStr,
                bestRoute.nextDistNameStr,
                prefix + bestRoute.pathDescription, // プレフィックスを適用
                totalDistanceStr
            };

        } else { // クランテレポートを考慮しない場合
            String totalDistanceP1Str = String.format("%.0f Blocks", routeFromP1.totalDistance);
            return new String[]{
                routeFromP1.nextDistCoordStr,
                routeFromP1.nextGoalCoordStr,
                routeFromP1.nextDistNameStr,
                routeFromP1.pathDescription,
                totalDistanceP1Str
            };
        }
    }

    public static void main(String[] args) {
        BestTeleport_mtb calculator = new BestTeleport_mtb(); // インスタンスを作成

        System.out.println("--- Test Case 1: プレイヤー開始 vs ClanTP1 vs ClanTP2 (ClanTP考慮あり) ---");
        // 仮定: ClanTP1 (0,64,0) からの方が目的地に近い
        //       プレイヤー (100,64,100), 目的地 (10,64,10)
        //       ClanTP2 (1000,70,1000) は遠い
        String[] route1 = calculator.getBestRoute(100, 64, 100, 10, 64, 10, true);
        System.out.printf("NextDist座標: %s%nNextGoal座標: %s%nNextDist名称: %s%nルート: %s%n総距離: %s%n", route1[0], route1[1], route1[2], route1[3], route1[4]);

        System.out.println("\n--- Test Case 2: プレイヤー開始のみ (ClanTP考慮なし) ---");
        String[] route2 = calculator.getBestRoute(100, 64, 100, 10, 64, 10, false);
        System.out.printf("NextDist座標: %s%nNextGoal座標: %s%nNextDist名称: %s%nルート: %s%n総距離: %s%n", route2[0], route2[1], route2[2], route2[3], route2[4]);

        System.out.println("\n--- Test Case 3: 複数テレポート & ClanTP2が有利なケース (ClanTP考慮あり) ---");
        // 最終目的地: メリアル城塞 (1319,127,-929)
        // PlayerStart: (0,64,0)
        // ClanTP1: (0,64,0) - PlayerStartと同じ
        // ClanTP2: イロエル (1290,125,-1092) - メリアル城塞に近い
        // 期待: clan tp 2 (イロエル) からの経路が選択され、メリアル城塞へは徒歩
        String[] route3 = calculator.getBestRoute(0, 64, 0, 1319, 127, -929, true);
        System.out.printf("NextDist座標: %s%nNextGoal座標: %s%nNextDist名称: %s%nルート: %s%n総距離: %s%n", route3[0], route3[1], route3[2], route3[3], route3[4]);
    }
}
