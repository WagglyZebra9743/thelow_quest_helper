package com.thelow_quest_helper.thelow_quest_helper.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
// Minecraft Forgeのイベントクラスをインポートしてください。
// Forge 1.8.9 の場合、以下のようなインポートが一般的です。
// 環境に合わせて調整してください。
import net.minecraftforge.fml.common.gameevent.TickEvent;



public class NextLocationBeacon {

    // 現在の案内情報
    private static int currentNextDistX, currentNextDistY, currentNextDistZ;
    private static int currentNextGoalX, currentNextGoalY, currentNextGoalZ;
    private static String currentNextDistName;

    // 最終目的地座標 (コマンド実行時に設定)
    private static int finalDestinationX, finalDestinationY, finalDestinationZ;

    private static boolean guidingPlayer = false; // 現在、特定の区間を案内中か
    private static volatile boolean toggleBeacon = false; // 機能全体のマスターオン/オフトグル

    private static final double REACH_THRESHOLD = 15; // 到達判定の閾値 (ブロック)
    private static BestTeleport_mtb routeCalculator = new BestTeleport_mtb(); // BestRouteクラスのインスタンス

    // コマンド送信タイマー関連
    private static int commandSendTimer = 0;
    private static final int COMMAND_SEND_INTERVAL = 400; // 10秒 (10秒 * 20tick/秒)

    /**
     * ビーコン案内システムを開始します。
     * /mtb besttp コマンドから最初の経路情報と共に呼び出されます。
     */
    public static void startGuiding(
            String initialNextDistCoordsStr, String initialNextGoalCoordsStr,
            String initialNextDistName,
            int finalDestX, int finalDestY, int finalDestZ) {

        finalDestinationX = finalDestX;
        finalDestinationY = finalDestY;
        finalDestinationZ = finalDestZ;
        currentNextDistName = initialNextDistName;

        try {
            int[] nextDistCoords = parseCoordinates(initialNextDistCoordsStr);
            currentNextDistX = nextDistCoords[0];
            currentNextDistY = nextDistCoords[1];
            currentNextDistZ = nextDistCoords[2];

            int[] nextGoalCoords = parseCoordinates(initialNextGoalCoordsStr);
            currentNextGoalX = nextGoalCoords[0];
            currentNextGoalY = nextGoalCoords[1];
            currentNextGoalZ = nextGoalCoords[2];

        } catch (IllegalArgumentException e) {
            System.err.println("[NextLocationBeacon] 初期座標のパースエラー: " + e.getMessage());
            setToggleBeacon(false); // エラー時はトグルオフ
            return;
        }

        double distToFinalFromInitialNextGoal = calculateDistanceSimple(currentNextGoalX, currentNextGoalY, currentNextGoalZ, finalDestinationX, finalDestinationY, finalDestinationZ);
        boolean alreadyAtFinalByInitialGoal = (distToFinalFromInitialNextGoal <= REACH_THRESHOLD);
        
        if (currentNextDistName.equals("目的地") && alreadyAtFinalByInitialGoal) {
            System.out.println("[NextLocationBeacon] 最初の案内が既に最終目的地です。");
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player != null) {
                 double playerDistToFinal = calculateDistanceSimple(player.posX, player.posY, player.posZ, finalDestinationX, finalDestinationY, finalDestinationZ);
                 if (playerDistToFinal <= REACH_THRESHOLD) {
                    System.out.println("[NextLocationBeacon] プレイヤーは既に最終目的地にいます。案内を終了します。");
                    setToggleBeacon(false);
                    return;
                 }
            }
        }
        
        setToggleBeacon(true);
        guidingPlayer = true;
        commandSendTimer = 0;
        activateBeacon();
        System.out.println("[NextLocationBeacon] 案内開始。最終目的地: (" + finalDestX + "," + finalDestY + "," + finalDestZ + ")");
    }

    /**
     * 現在の nextDist にビーコンを表示（または更新）します。
     */
    private static void activateBeacon() {
        if (guidingPlayer && toggleBeacon) {
            System.out.println("[NextLocationBeacon] ビーコン表示: " + currentNextDistName +
                               " @ (" + currentNextDistX + "," + currentNextDistY + "," + currentNextDistZ + "). 次の目標: ("+currentNextGoalX+","+currentNextGoalY+","+currentNextGoalZ+").");
            // 実際のBeaconBeamDrawerクラスのメソッドを呼び出す
            // 例: com.Sura.Matatabi.events.BeaconBeamDrawer.displayPoint(currentNextDistX, currentNextDistY, currentNextDistZ, currentNextDistName);
            // もし events.BeaconBeamDrawer が別パッケージなら、適切なインポートと呼び出しが必要です。
            // ここでは仮に events.BeaconBeamDrawer.displayPoint としています。
            com.thelow_quest_helper.thelow_quest_helper.item.BeaconBeamDrawer.displayPoint(currentNextDistX, currentNextDistY, currentNextDistZ, currentNextDistName);
        } else {
            // 実際のBeaconBeamDrawerクラスのメソッドを呼び出す
        	com.thelow_quest_helper.thelow_quest_helper.item.BeaconBeamDrawer.clearGuidanceBeam();
        }
    }

    /**
     * ビーコン機能全体のトグル状態を設定します。
     */
    public static void setToggleBeacon(boolean enabled) {
        boolean previousToggleState = toggleBeacon;
        toggleBeacon = enabled;
        if (!toggleBeacon) {
            guidingPlayer = false;
            com.thelow_quest_helper.thelow_quest_helper.item.BeaconBeamDrawer.clearGuidanceBeam();
            if (previousToggleState) {
                 System.out.println("[NextLocationBeacon] toggleBeacon が false に設定されました。全ての案内を停止します。");
            }
        } else { // toggleBeacon is true
            if (!previousToggleState && guidingPlayer) { // falseからtrueになり、かつ案内中だった場合（再開のシナリオ）
                // guidingPlayer が true ということは、停止前に案内していた区間情報が残っている
                activateBeacon(); // ビーコンを再表示
            }
        }
    }
    
    public static boolean isToggleBeaconActive() {
        return toggleBeacon;
    }

    /**
     * 文字列 "(x,y,z)" を int 配列にパースします。
     */
    private static int[] parseCoordinates(String coords) throws IllegalArgumentException {
        if (coords == null) throw new IllegalArgumentException("座標文字列がnullです。");
        coords = coords.replace("(", "").replace(")", "");
        String[] parts = coords.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("座標文字列のフォーマットエラー: " + coords);
        }
        try {
            return new int[]{
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim())
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("座標文字列に数値以外の文字が含まれています: " + coords, e);
        }
    }
    
    /**
     * 次の経路区間を計算し、ビーコンを更新します。
     */
    private static void updateRouteAndBeacon(double playerX, double playerY, double playerZ) {
        System.out.println("[NextLocationBeacon] 次の区間を計算中: (" + (int)playerX + "," + (int)playerY + "," + (int)playerZ + 
                           ") から (" + finalDestinationX + "," + finalDestinationY + "," + finalDestinationZ + ") へ");

        String[] nextRouteInfo = routeCalculator.getBestRoute(
                (int) Math.round(playerX), (int) Math.round(playerY), (int) Math.round(playerZ),
                finalDestinationX, finalDestinationY, finalDestinationZ,
                true // clanTP の使用は常に true で次の経路を計算
        );

        // nextRouteInfo: {nextDist座標, nextGoal座標, nextDistName, 移動形式, 総距離}
        // routeInfo[3] は pathDescription (BestRouteの戻り値のインデックスに注意)
        if (nextRouteInfo[2].contains("到達可能な経路が見つかりません") || nextRouteInfo[2].equals("出発地と目的地が同じです")) {
            // BestRouteの戻り値のインデックスは 0:nextDistCoord, 1:nextGoalCoord, 2:nextDistName, 3:pathDesc, 4:totalDist
            // よって、pathDescriptionは nextRouteInfo[3]
            if (nextRouteInfo[3].contains("到達可能な経路が見つかりません")) {
                 System.out.println("[NextLocationBeacon] 最終目的地への経路が見つかりません。");
            } else if (nextRouteInfo[3].equals("出発地と目的地が同じです")) {
                 System.out.println("[NextLocationBeacon] プレイヤーは実質的に最終目的地に到達しました (BestRoute判断)。");
            }
            setToggleBeacon(false);
            return;
        }

        try {
            int[] nextDistCoords = parseCoordinates(nextRouteInfo[0]);
            int[] nextGoalCoords = parseCoordinates(nextRouteInfo[1]);

            currentNextDistX = nextDistCoords[0];
            currentNextDistY = nextDistCoords[1];
            currentNextDistZ = nextDistCoords[2];
            currentNextGoalX = nextGoalCoords[0];
            currentNextGoalY = nextGoalCoords[1];
            currentNextGoalZ = nextGoalCoords[2];
            currentNextDistName = nextRouteInfo[2]; // nextDistName はインデックス2
            
            guidingPlayer = true; 
            activateBeacon();

        } catch (IllegalArgumentException e) {
            System.err.println("[NextLocationBeacon] 次区間の座標パースエラー: " + e.getMessage());
            setToggleBeacon(false);
        }
    }

    private static double calculateDistanceSimple(double x1, double y1, double z1, int x2, int y2, int z2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
    }

    // --- イベントハンドラメソッド群 ---
    // これらはForgeのイベントバスに適切に登録される必要があります。

    /**
     * プレイヤーのティック更新ごとに呼び出され、目標到達をチェックします。
     * (TickEvent.PlayerTickEvent ハンドラから呼び出す)
     */
    

    /**
     * クライアントのティック更新ごとに呼び出され、コマンド送信タイマーを処理します。
     * (TickEvent.ClientTickEvent ハンドラから呼び出す)
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) { // STARTフェーズでは処理しない (ENDでまとめて処理)
            // return;
        }
        // System.out.println("a");
        // 1. コマンド送信タイマー処理
        if (toggleBeacon) {
            commandSendTimer++;
            if (commandSendTimer >= COMMAND_SEND_INTERVAL) {
                commandSendTimer = 0;
                EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                if (player != null) {
                    System.out.println("[NextLocationBeacon] コマンド送信: /thelow_api locate");
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/thelow_api location");
                }
            }
        } else {
            commandSendTimer = 0; 
        }

        // 2. プレイヤーの目標到達判定と経路更新処理
        // ★デバッグログ追加: 判定処理の前の主要な状態を出力
        // System.out.println(String.format("[NextLocationBeacon DEBUG ClientTick] Pre-Check: toggleBeacon: %b, guidingPlayer: %b", toggleBeacon, guidingPlayer));

        if (!toggleBeacon || !guidingPlayer) {
            return; // マスターオフ、または特定の区間案内中でなければ何もしない
        }

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return;
        }

        double distanceToCurrentNextGoal = calculateDistanceSimple(player.posX, player.posY, player.posZ, currentNextGoalX, currentNextGoalY, currentNextGoalZ);
        
        // ★デバッグログ追加: 毎ティックの距離情報を出力
        /* System.out.println(String.format("[NextLocationBeacon DEBUG ClientTick] Player Pos: (%.1f, %.1f, %.1f), CurrentNextGoal: (%d,%d,%d), Distance: %.2f, Threshold: %.1f",
            player.posX, player.posY, player.posZ, currentNextGoalX, currentNextGoalY, currentNextGoalZ, distanceToCurrentNextGoal, REACH_THRESHOLD)); */


        if (distanceToCurrentNextGoal <= REACH_THRESHOLD) {
            System.out.println("[NextLocationBeacon DEBUG ClientTick] Player REACHED currentNextGoal: (" + currentNextGoalX + "," + currentNextGoalY + "," + currentNextGoalZ + ")");
            
            double distanceOfNextGoalToFinalDest = calculateDistanceSimple(currentNextGoalX, currentNextGoalY, currentNextGoalZ, finalDestinationX, finalDestinationY, finalDestinationZ);
            System.out.println("[NextLocationBeacon DEBUG ClientTick] currentNextGoal is (" + currentNextGoalX + "," + currentNextGoalY + "," + currentNextGoalZ + ")");
            System.out.println("[NextLocationBeacon DEBUG ClientTick] finalDestination is (" + finalDestinationX + "," + finalDestinationY + "," + finalDestinationZ + ")");
            System.out.println("[NextLocationBeacon DEBUG ClientTick] Distance from currentNextGoal to finalDestination: " + distanceOfNextGoalToFinalDest);


            if (distanceOfNextGoalToFinalDest <= REACH_THRESHOLD) {
                System.out.println("[NextLocationBeacon DEBUG ClientTick] Final destination reached condition MET.");
                System.out.println("[NextLocationBeacon] プレイヤーが最終目的地に到達しました！");
                setToggleBeacon(false); 
            } else {
                System.out.println("[NextLocationBeacon DEBUG ClientTick] Final destination reached condition NOT MET. Updating route...");
                updateRouteAndBeacon(player.posX, player.posY, player.posZ);
            }
        }
    }

    /**
     * 受信したチャットメッセージを監視し、特定の文字列が含まれていたらトグルをオフにします。
     * (ClientChatReceivedEvent ハンドラから呼び出す)
     */
    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        // event.type (byte): 0 は通常チャットメッセージ, 1 はシステムメッセージ, 2 は ActionBar
        if (toggleBeacon && event.message != null && (event.type == 0 || event.type == 1)) {
            String messageText = event.message.getUnformattedText();
            if (messageText.contains("$api") && messageText.contains("dungeon")) {
                System.out.println("[NextLocationBeacon] チャットから '$api' と 'dungeon' を検出。案内を停止します。");
                setToggleBeacon(false);
            }
        }
    }
}