package com.ga170212.chk_datapack_link.chzzk;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class ChzzkMessageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger("chk-datapack-link/ChzzkMessageProcessor");
    private static final Pattern EMOJI_PATTERN = Pattern.compile("\\{:[^\\}:]+:\\}");

    public record ParsedCmdChat(String cmd, String chat, java.util.List<String> args) {}

    public record ChatData(
            String nickname,
            String userId,
            String rawMessage,
            String cmd,
            String chat,
            java.util.List<String> args,
            long msgTime
    ) {}

    public record DonationData(
            String nickname,
            String userId,
            int amount,
            String payType,
            String rawMessage,
            String cmd,
            String chat,
            java.util.List<String> args,
            long msgTime
    ) {}

    public interface MessageHandler {
        void onChatReceived(ChatData chatData);
        void onDonationReceived(DonationData donationData);
    }

    /**
     * 치지직 전용 이모티콘 태그(예: {:d_108:})를 제거하고 텍스트를 정제합니다.
     */
    public static String cleanEmojiTokens(String message) {
        if (message == null) return "";
        return EMOJI_PATTERN.matcher(message).replaceAll("").trim();
    }

    /**
     * 입력 메시지에서 !명령어와 본문(chat), 그리고 쉼표(,) 구분 인자 목록(args)을 분리합니다.
     * !가 없더라도 쉼표(,)가 있으면 항상 args 목록(arg0, arg1, ...)으로 분할됩니다.
     */
    public static ParsedCmdChat parseCommandAndChat(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new ParsedCmdChat("", "", java.util.Collections.emptyList());
        }
        String trimmed = message.trim();
        String cmd = "";
        String chat = "";

        if (trimmed.startsWith("!")) {
            String withoutExclamation = trimmed.substring(1).trim();
            if (withoutExclamation.isEmpty()) {
                return new ParsedCmdChat("", "", java.util.Collections.emptyList());
            }
            String[] parts = withoutExclamation.split("\\s+", 2);
            cmd = parts[0];
            chat = parts.length > 1 ? cleanEmojiTokens(parts[1]) : "";
        } else {
            chat = cleanEmojiTokens(trimmed);
        }

        java.util.List<String> argsList = new java.util.ArrayList<>();
        if (!chat.isEmpty()) {
            String[] splitArgs = chat.split(",");
            for (String arg : splitArgs) {
                argsList.add(arg.trim());
            }
        }

        return new ParsedCmdChat(cmd, chat, argsList);
    }

    /**
     * 치지직 웹소켓 수신 패킷(JSON)을 해석하여 메시지 핸들러로 전달합니다.
     */
    public static void processPacket(String jsonMessage, MessageHandler handler) {
        if (jsonMessage == null || jsonMessage.trim().isEmpty()) return;

        try {
            JsonObject root = JsonParser.parseString(jsonMessage).getAsJsonObject();
            if (!root.has("cmd")) return;

            int cmd = root.get("cmd").getAsInt();

            // 핑퐁 및 채널 입장 확인 패킷 스킵
            if (cmd == 0 || cmd == 10000 || cmd == 10100) {
                return;
            }

            // 일반 채팅(93101) 및 후원(93102) 메시지 처리
            if (cmd == 93101 || cmd == 93102) {
                if (!root.has("bdy")) return;
                JsonElement bdyElem = root.get("bdy");

                JsonArray bdyArray = new JsonArray();
                if (bdyElem.isJsonArray()) {
                    bdyArray = bdyElem.getAsJsonArray();
                } else if (bdyElem.isJsonObject()) {
                    bdyArray.add(bdyElem.getAsJsonObject());
                }

                for (JsonElement elem : bdyArray) {
                    if (!elem.isJsonObject()) continue;
                    JsonObject item = elem.getAsJsonObject();

                    int msgTypeCode = getAsInt(item, "msgTypeCode", cmd == 93102 ? 10003 : 10001);
                    String rawMsg = getAsString(item, "msg", "");
                    long msgTime = getAsLong(item, "msgTime", System.currentTimeMillis());

                    // 프로필 (닉네임, 유저 아이디) 추출
                    String nickname = "익명";
                    String userId = "";
                    if (item.has("profile") && !item.get("profile").isJsonNull()) {
                        JsonObject profileObj = parseNestedJsonObject(item.get("profile"));
                        if (profileObj != null) {
                            nickname = getAsString(profileObj, "nickname", "익명");
                            userId = getAsString(profileObj, "userIdHash", "");
                        }
                    }

                    ParsedCmdChat parsed = parseCommandAndChat(rawMsg);
                    String cleanedRawMsg = cleanEmojiTokens(rawMsg);

                    boolean isDonation = (cmd == 93102) || (msgTypeCode == 10003) || (msgTypeCode == 10010);

                    // 1. 후원 메시지 수신 (cmd 93102 또는 msgTypeCode 10003, 10010)
                    if (isDonation) {
                        int amount = 0;
                        String payType = "CHEESE";

                        if (item.has("extras") && !item.get("extras").isJsonNull()) {
                            JsonObject extrasObj = parseNestedJsonObject(item.get("extras"));
                            if (extrasObj != null) {
                                amount = getAsInt(extrasObj, "payAmount", getAsInt(extrasObj, "cheeseAmount", 0));
                                payType = getAsString(extrasObj, "payType", "CHEESE");
                            }
                        }

                        DonationData donationData = new DonationData(
                                nickname,
                                userId,
                                amount,
                                payType,
                                cleanedRawMsg,
                                parsed.cmd(),
                                parsed.chat(),
                                parsed.args(),
                                msgTime
                        );
                        LOGGER.info("Donation -> Nickname: {}, Amount: {}, Cmd: '{}', Args: {}", nickname, amount, parsed.cmd(), parsed.args());
                        handler.onDonationReceived(donationData);

                    // 2. 일반 채팅 수신 (msgTypeCode 10001)
                    } else {
                        ChatData chatData = new ChatData(
                                nickname,
                                userId,
                                cleanedRawMsg,
                                parsed.cmd(),
                                parsed.chat(),
                                parsed.args(),
                                msgTime
                        );
                        LOGGER.info("Chat -> Nickname: {}, Cmd: '{}', Args: {}", nickname, parsed.cmd(), parsed.args());
                        handler.onChatReceived(chatData);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse Chzzk packet: {}", e.getMessage());
        }
    }

    // --- JSON 읽기 헬퍼 메서드 (가독성 및 유지보수용) ---

    private static JsonObject parseNestedJsonObject(JsonElement element) {
        try {
            if (element.isJsonObject()) {
                return element.getAsJsonObject();
            } else if (element.isJsonPrimitive()) {
                return JsonParser.parseString(element.getAsString()).getAsJsonObject();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String getAsString(JsonObject obj, String key, String defaultValue) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : defaultValue;
    }

    private static int getAsInt(JsonObject obj, String key, int defaultValue) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsInt() : defaultValue;
    }

    private static long getAsLong(JsonObject obj, String key, long defaultValue) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsLong() : defaultValue;
    }
}
