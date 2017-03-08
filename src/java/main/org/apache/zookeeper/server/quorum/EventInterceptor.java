/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zookeeper.server.quorum;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;


public class EventInterceptor{
    private static final Logger LOG = LoggerFactory.getLogger(EventInterceptor.class);
    String ipcDir="/tmp/ipc";
    int sendNode;
    int recvNode;
    int nodeState;
    int eventId;
    int leaderId;
    int zxid=0;
    String filename;

    EventInterceptor(QuorumPeer learner, QuorumPacket packet){  //Learner.java
        this.sendNode=(int)learner.getId();
        this.recvNode=(int)learner.getCurrentVote().getId();
        this.leaderId= (int)learner.getCurrentVote().getId();
        this.nodeState=learner.getPeerState().getValue();
        this.zxid= (int) packet.getZxid();
        this.eventId = getEventId();
        this.filename="sync-"+Long.toString(eventId);
        try {
            PrintWriter writer = new PrintWriter(ipcDir+"/new/"+filename);
            writer.println("sender="+this.sendNode);
            writer.println("recv="+this.recvNode);
            writer.println("leader="+this.leaderId);
            writer.println("state="+this.nodeState);
            writer.println("strSendRole="+learner.getServerState());
            writer.println("zxid="+this.zxid);
            writer.close();
            LOG.info("[updateDMCK] sender-"+learner.getId()+" sendRole-"+learner.getServerState()+" leader-"+learner.getCurrentVote().getId());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        commitEvent();
        waitAck();

    }
    EventInterceptor(QuorumPeer leader, QuorumPacket packet, long recv){  //LearnerHandler.java
        this.sendNode= (int)leader.getId();
        this.recvNode=(int) recv;
        this.leaderId=(int) leader.getId();
        this.nodeState=leader.getPeerState().getValue();
        this.zxid= (int) packet.getZxid();
        this.eventId = getEventId();
        this.filename="sync-"+Long.toString(eventId);
        try {
            PrintWriter writer = new PrintWriter(ipcDir+"/new/"+filename);
            writer.println("sender="+this.sendNode);
            writer.println("recv="+this.recvNode);
            writer.println("leader="+this.leaderId);
            writer.println("state="+this.nodeState);
            writer.println("strSendRole="+leader.getServerState());
            writer.println("zxid="+packet.getZxid());
            writer.close();
            LOG.info("[updateDMCK] sender-"+leader.getId()+" sendRole-"+leader.getServerState()+" leader-"+leader.getCurrentVote().getId());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        commitEvent();
        waitAck();

    }


    EventInterceptor(QuorumPeer peer){
        this.sendNode= (int)peer.getId();
        this.recvNode=(int)peer.getId();
        this.leaderId = -1;
        this.nodeState = 4;
        this.eventId=getEventId();
        this.filename="zkls-"+Long.toString(eventId);
        try {
            PrintWriter writer = new PrintWriter(ipcDir+"/new/"+filename);
            writer.println("sender="+this.sendNode);
            writer.println("recv="+this.recvNode);
            writer.println("proposedLeader="+this.leaderId);
            writer.println("state="+this.nodeState);
            writer.println("strSendRole=CRASHED");
            writer.close();
            LOG.info("[updateDMCK] sender-"+peer.getId()+" sendRole-Crashed");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        commitEvent();
    }

    EventInterceptor(long leader, long sender, QuorumPeer.ServerState state){
        this.sendNode=(int) sender;
        this.recvNode=(int) sender;
        this.nodeState=state.getValue();
        this.leaderId=(int) leader;
        this.eventId=getEventId();
        this.filename="zkls-"+Long.toString(eventId);
        try {
            PrintWriter writer = new PrintWriter(ipcDir+"/new/"+filename);
            writer.println("sender="+this.sendNode);
            writer.println("recv="+this.recvNode);
            writer.println("state="+this.nodeState);
            writer.println("strSendRole="+state);
            writer.println("leader="+this.leaderId);
            writer.close();
            LOG.info("[updateDMCK] sender-"+sender+" sendRole-"+state+" leader-"+leader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        commitEvent();
    }

//    EventInterceptor(long leader, HashMap<Long, Vote> recvset, long sender, QuorumPeer.ServerState state, long myid, long xx){
//        this.eventId=myid;
//        this.filename="zkls-"+Long.toString(eventId);
//        try {
//            PrintWriter writer = new PrintWriter(ipcDir+"/new/"+myid);
//            writer.print("sender="+sender);
//            writer.print("state="+state.toString());
//            writer.print("strSendRole="+state);
//            writer.print("leader="+leader);
//            writer.print("electionTable=");
//            for (long node: recvset.keySet()){
//                writer.print(node+":"+recvset.get(node).getId()+",");
//            }
//            writer.close();
//            System.out.println("[updateDMCK] sender-"+sender+" sendRole-"+state+" leader-"+leader);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        commitEvent();
//        waitAck();
//    }
//
//    EventInterceptor(long leader, HashMap<Long, Vote> recvset, long sender, QuorumPeer.ServerState state, long myid){
//        this.eventId=myid;
//        this.filename="zkls-"+Long.toString(eventId);
//        try {
//            PrintWriter writer = new PrintWriter(ipcDir+"/new/"+filename);
//            writer.print("sender="+sender);
//            writer.print("state="+state.toString());
//            writer.print("strSendRole="+state);
//            writer.print("leader="+leader);
//            writer.print("electionTable=");
//            for (long node: recvset.keySet()){
//                writer.print(node+":"+recvset.get(node).getId()+",");
//            }
//            writer.close();
//            System.out.println("[updateDMCK] sender-"+sender+" sendRole-"+state+" leader-"+leader);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        commitEvent();
//    }
//
//    EventInterceptor(long sender, long recv, QuorumPeer.ServerState state,long leader,long zxid, long electionEpoch, long peerEpoch ){
//        this.eventId=getHash(sender, recv);
//        this.filename="zk-"+Long.toString(eventId);
//        try {
//            PrintWriter writer = new PrintWriter(ipcDir+"/new/"+filename);
//            writer.println("sender="+sender);
//            writer.println("recv="+recv);
//            writer.println("state="+state.getValue());
//            writer.println("strSendRole="+state);
//            writer.println("leader="+leader);
//            writer.println("zxid="+zxid);
//            writer.println("epoch="+electionEpoch);
//            writer.println("peerEpoch="+peerEpoch);
//            writer.close();
//            System.out.println("[updateDMCK] sender-"+sender+" sendRole-"+state+" leader-"+leader);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        commitEvent();
//        waitAck();
//    }

    public void commitEvent(){
        try {
            LOG.info("@hk commit filename -"+filename);
            Runtime.getRuntime().exec("mv "+ipcDir+"/new/"+filename+" "+ipcDir+"/send/"+filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getHash(int from, int to){
        final int prime=19;
        int hash=1;
        hash=prime*hash+to;
        hash=prime*hash+from;
        //result=prime*result+**
        return hash;
    }

    public int getEventId(){
        final int prime=19;
        int hash=1;
        hash=prime*hash+this.sendNode;
        hash=prime*hash+this.recvNode;
        hash=prime*hash+this.leaderId;
        hash=prime*hash+this.nodeState;
        return hash;
    }


    public void waitAck(){
        String ackFileName = ipcDir+"/ack/"+filename;
        File ackFile= new File(ackFileName);
        System.out.println("ack file : "+ackFile.getAbsolutePath());
        while (!ackFile.exists()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LOG.info("@hk /ack file"+ ackFileName + " exist");
        try {
            Runtime.getRuntime().exec("rm "+ipcDir+"/ack/"+filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}