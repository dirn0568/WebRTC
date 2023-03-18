package com.example.webrtckw.handler;

import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SignalHandler extends TextWebSocketHandler {
    List<HashMap<String, Object>> sessions = new ArrayList<>(); // 세션 저장 + 세션에 메세지 전달 하기 위한 용도
    static int roomIndex = -1;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //메시지 발송 // 구독 개념이 아닌 세션들을 찾아서 찾은 세션들한테만 보내주는 개념인듯함
        System.out.println("메세지 받음??????");

        String msg = message.getPayload();
        JSONObject obj = jsonToObjectParser(msg);

        String roomNumber = (String) obj.get("roomNumber");
        String msgType = (String) obj.get("type");
        HashMap<String, Object> sessionMap = new HashMap<String, Object>();

        if (sessions.size() > 0) {
            for (int i=0; i<sessions.size(); i++) {
                String tempRoomNumber = (String) sessions.get(i).get("roomNumber");
                if (roomNumber.equals(tempRoomNumber)) {
                    sessionMap = sessions.get(i);
                    roomIndex = i;
                    break;
                }
            }

            if(!msgType.equals("file")) {
                // 해당 방에 있는 세션들에만 메세지 전송
                for (String sessionMapKey: sessionMap.keySet()) {
                    if(sessionMapKey.equals("roomNumber")) { // 다만 방번호일 경우에는 건너뛴다.
                        continue;
                    }

                    WebSocketSession webSocketSession = (WebSocketSession) sessionMap.get(sessionMapKey); // webSocketSession == StandardWebSocketSession[id=9b6f63f7-c1b1-9a09-5110-1fdff5973f86, uri=ws://localhost:8080/chating/abc]
                    webSocketSession.sendMessage(new TextMessage(obj.toJSONString()));
                }
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //소켓 연결
        System.out.println("연결요청 옴????");

        boolean sessionExist = false;

        String sessionUrl = session.getUri().toString();
        String roomNumber = sessionUrl.split("/signal/")[1];
        int roomIndex = -1;
        if (sessions.size() > 0) {
            for (int i=0; i<sessions.size(); i++) {
                String tempRoomNumber = (String) sessions.get(i).get("roomNumber");
                if (roomNumber.equals(tempRoomNumber)) {
                    sessionExist = true;
                    roomIndex = i;
                    break;
                }
            }
        }

        if (sessionExist) {
            HashMap<String, Object> sessionMap = sessions.get(roomIndex);
            sessionMap.put(session.getId(), session);
        } else {
            HashMap<String, Object> sessionMap = new HashMap<String, Object>();
            sessionMap.put("roomNumber", roomNumber);
            sessionMap.put(session.getId(), session);
            sessions.add(sessionMap);
        }

        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //소켓 종료
        System.out.println("연결 닫는 요청 옴????");

        if(sessions.size() > 0) { //소켓이 종료되면 해당 세션값들을 찾아서 지운다.
            for (HashMap<String, Object> stringObjectHashMap : sessions) {
                stringObjectHashMap.remove(session.getId());
            }
        }
        super.afterConnectionClosed(session, status);
    }

    private static JSONObject jsonToObjectParser(String jsonStr) {
        JSONParser parser = new JSONParser();
        JSONObject obj = null;
        try {
            obj = (JSONObject) parser.parse(jsonStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return obj;
    }
}