package com.ssafy.gaese.domain.typing.controller;


import com.ssafy.gaese.domain.typing.application.IsPlay;
import com.ssafy.gaese.domain.typing.application.TypingRoomApp;
import com.ssafy.gaese.domain.typing.application.TypingUserApp;
import com.ssafy.gaese.domain.typing.common.TypingStaticData;
import com.ssafy.gaese.domain.typing.dto.*;
import com.ssafy.gaese.domain.user.entity.User;
import com.ssafy.gaese.domain.user.exception.UserNotFoundException;
import com.ssafy.gaese.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
//@RequestMapping("/typing")
@RequiredArgsConstructor
public class TypingRoomController {

    @Autowired
    TypingUserApp typingUserApp;

    @Autowired
    TypingRoomApp typingRoomApp;


    //나중에 요청해서 service에 기능 만들어서 쓰는게 좋을듯함
    @Autowired
    UserRepository userRepository;

    @Autowired
    IsPlay isPlay;

    private final SimpMessageSendingOperations sendingOperations;





    @MessageMapping("/typing/enter")
    public void enter(EnterParamDto param) throws InterruptedException {
        EnterResultDto resultDto = new EnterResultDto();
        //방 만드는 경우 사용할 예정
        TypingRoom typingRoom = new TypingRoom();

        TypingUser typingUser = new TypingUser();

        //이미 플레이 중인지 체크
        //true 면 플레이 중인거임
        if(isPlay.isPlayCheck(param.getNickName()))
        {
            resultDto.setPlay(false);

            //이거 http 정해야함
//            return new ResponseEntity<EnterResultDto>(resultDto, HttpStatus.OK);
            sendingOperations.convertAndSend("/topic/typing/"+param.getId()+"/enter", resultDto);
        }


        resultDto.setPlay(true);
        resultDto.setRoomCodeCheck(true);
        resultDto.setUsers(new ArrayList<EnterUserDto>());



        User userE = userRepository.findById(param.getId())
                .orElseThrow((()->new UserNotFoundException()));

        typingUser.setImgUrl(userE.getImg());
        typingUser.setNickName(param.getNickName());
        typingUser.setSocketId(param.getSocketId());
        typingUserApp.setUser(typingUser);

        //친선전 참가하는 경우, null이 아니면 친선전참가
        if(param.getRoomCode()!=null)
        {
            resultDto.setRoomNo(typingRoomApp.getRoomCodeToRoomNo(param.getRoomCode()));
            resultDto.setRoomCode(param.getRoomCode());
            //roomCode가 잘못된 코드인 경우
            if(resultDto.getRoomCode()==null)
            {
                resultDto.setRoomCodeCheck(false);
//                return new ResponseEntity<EnterResultDto>(resultDto, HttpStatus.OK);
                sendingOperations.convertAndSend("/topic/typing/"+param.getId()+"/enter", resultDto);
            }

            typingRoomApp.enterUser(typingUser,resultDto.getRoomCode());



//			return new ResponseEntity<EnterResultDto>(resultDto, HttpStatus.OK);
        }
        //친선전 만드는 경우 true 면 방생성
        else if(param.isCreat())
        {
            //중복코드 생성 될 수도있음 나중에 처리 해줘야함
            //룸코드 생성
            typingRoom.setRoomCode(TypingStaticData.roomCodeMaker());
            resultDto.setRoomCode(typingRoom.getRoomCode());

            //나중에 db에서 가져와서 넣는 로직 만들어 줘야함
            typingRoom.setContent("test");

            resultDto.setRoomNo(param.getSocketId());
            typingRoom.setRoomNo(param.getSocketId());
            typingRoom.setLang(param.getLang());
            typingRoom.setStartTime(TypingStaticData.timeMaker());

            //방 만든 다음 유저 넣어야 유저 리스트에 추가됨
            typingRoomApp.makeRoom(typingRoom);
            typingRoomApp.enterUser(typingUser, resultDto.getRoomCode());
        }
        //랜덤매칭 하는 경우
        else
        {

            typingRoomApp.enterUser(typingUser, null);

        }

        typingUserApp.setUser(typingUser);

        List<String> nickList = typingRoomApp.getUserList(resultDto.getRoomNo());

        for (String nick: nickList)
        {
            EnterUserDto eud = new EnterUserDto();
            eud.setImgUrl(typingUserApp.getVar(nick,"imgUrl"));
            eud.setNickName(typingUserApp.getVar(nick,nick));
            eud.setHead(Boolean.parseBoolean(typingUserApp.getVar(nick,"isHead")));
            resultDto.getUsers().add(eud);
        }
        sendingOperations.convertAndSend("/topic/typing/"+resultDto.getRoomNo()+"/userList", resultDto.getUsers());

        //나중에 타이머 걸어서 구현 하고 일단은 바로 시작하게 만듬
        int maxSize=4;
        if(resultDto.getUsers().size()==maxSize)
        {
            StartResultDto contentDto = new StartResultDto();
            String[] contens =typingRoomApp.getVar(resultDto.getRoomNo(), "content").split("\n");
            contentDto.setContent(Arrays.asList(contens));


            sendingOperations.convertAndSend("/topic/typing/"+param.getId()+"/enter", resultDto);
            Thread.sleep(100);
            typingRoomApp.setVar(resultDto.getRoomNo(),"startTime",TypingStaticData.timeMaker()+"");
            sendingOperations.convertAndSend("/topic/typing/"+resultDto.getRoomNo()+"/start", resultDto.getUsers());
        }
        else
        {
            sendingOperations.convertAndSend("/topic/typing/"+param.getId()+"/enter", resultDto);
        }
//        return new ResponseEntity<EnterResultDto>(resultDto, HttpStatus.OK);
    }




    @MessageMapping("/typing/check")
    public void ready(CheckParamDto paramDto) throws Exception {


        ScoreResultDto resultDto = new ScoreResultDto();
        resultDto.setUsers(new ArrayList<>());


//        String RoomNo = typingRoomApp.getRoomNoToNickName(paramDto.getNickName());
        String content =typingRoomApp.getVar(paramDto.getRoomNo(),"content");
        String[] contents = content.split("\n");
        int contentLen = content.length()-(contents.length-1)*2;
        int startTime =Integer.parseInt(typingRoomApp.getVar(paramDto.getRoomNo(),"startTime"));
        List<String> nickList = typingRoomApp.getUserList(paramDto.getRoomNo());

        boolean endCheck =false;
        int maxProgress =0;
        for (String nick: nickList)
        {
            ScoreUserDto scoreUserDto = new ScoreUserDto();
            int trues =Integer.parseInt(typingUserApp.getVar(nick,"trues"));
            int errors =Integer.parseInt(typingUserApp.getVar(nick,"errors"));

            if(paramDto.isCheck())
                trues++;
            else
                errors++;

            int nowTime = TypingStaticData.timeMaker();

            int time =startTime-nowTime;
            if(time<0)
                time+=3600;

            scoreUserDto.setProgress(((trues+errors)*100)/contentLen);
            scoreUserDto.setErrors(errors);
            scoreUserDto.setNickName(nick);
            scoreUserDto.setTypeSpeed((trues*60)/time);
            if(scoreUserDto.getProgress()==100)
                endCheck=true;
            if(maxProgress<scoreUserDto.getProgress())
            {
                maxProgress=scoreUserDto.getProgress();
                scoreUserDto.setRank(1);
            }

            resultDto.getUsers().add(scoreUserDto);
        }

        for (int i=2; i<=resultDto.getUsers().size();i++)
        {
            int sec=0;
            for (int j=0; j<=resultDto.getUsers().size();j++)
            {
                int now = resultDto.getUsers().get(j).getProgress();
                if(maxProgress > now && sec < now)
                {
                    sec=now;
                    resultDto.getUsers().get(j).setRank(i);
                }
            }
            maxProgress=sec;
        }

        sendingOperations.convertAndSend("/topic/typing/"+paramDto.getRoomNo()+"/score", resultDto);

        if(endCheck)
            sendingOperations.convertAndSend("/topic/typing/"+paramDto.getRoomNo()+"/end", resultDto);

        return;
    }


}