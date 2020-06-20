/*
 * This file is part of the OdinMS MapleStory Private Server
 * Copyright (C) 2011 Patrick Huy and Matthias Butz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package client;

import scripting.NPCConversationManager;
import scripting.NPCScriptManager;
import server.Randomizer;
import server.SpeedQuizFactory;
import server.SpeedQuizFactory.QuizEntry;
import tools.MaplePacketCreator;

import java.util.List;

/**
 * @author AuroX
 */
public class SpeedQuiz {

  private final int INITIAL_QUESTION = 50; // Default 50 questions.
  private final int TIME = 15; // Default 15 seconds
  private final int npc;
  private final byte type; // 0 = npc, 1 = mob, 3 = item
  private int question;
  private int points;
  private String answer;

  public SpeedQuiz(final MapleClient c, final int npc) {
    this.question = INITIAL_QUESTION;
    this.points = 0;
    this.answer = null;
    this.npc = npc;
    this.type = (byte) Randomizer.nextInt(3); // 0 = npc, 1 = mob, 3 = item
    //c.getPlayer().gainMeso(-1000, true, true, true); // Set the time here..like time limit?
    getNewQuestion(c, question);
  }

  public final void nextRound(final MapleClient c, final String answerGiven) {
    if (answerGiven.equals("__GIVEUP__")) {
      giveUp(c);
      return;
    }
    CheckAnswer(answerGiven);
    if (question == 1) { // This is the last question already..
      reward(c);
      return;
    }
    question--;
    getNewQuestion(c, question);
  }

  private void getNewQuestion(final MapleClient c, final int questionNo) {
    final NPCConversationManager cm = NPCScriptManager.getInstance().getCM(c);
    if (cm.getNpc() != npc) {
      //System.out.println("NPC ID IS DIFFERENT");
      return;
    }

    final List<QuizEntry> entries = SpeedQuizFactory.getInstance().getQuizDataType(questionNo, type);
    final QuizEntry random = entries.get(Randomizer.nextInt(entries.size()));

    this.answer = random.getAnswer();
    //System.out.println(answer);

    c.getSession().write(MaplePacketCreator.getSpeedQuiz(npc, random.getType(), random.getObjectId(), points, questionNo, TIME));
    cm.setLastMsg((byte) 7);
  }

  private void CheckAnswer(final String answerGiven) {
    if (answerGiven == null || answerGiven.equals("")) {
      return;
    } else if (answer.equalsIgnoreCase(answerGiven)) { // Loosen a bit i guess...let them any case also can.
      points++;
    }
  }

  private void giveUp(final MapleClient c) {
    final NPCConversationManager cm = NPCScriptManager.getInstance().getCM(c);
    if (points > 0) {
      c.getPlayer().gainMeso(100 * points, true, true, true); // todo change reward? give up = lesser rewards
    }
    cm.sendNext("Ahhh...Its sad that you're giving up the quiz although you managed to answer " + points + " questions. Here's some mesos as a token of appreciation from me.");
    cm.dispose();
    c.getPlayer().setSpeedQuiz(null);
  }

  private void reward(final MapleClient c) {
    final NPCConversationManager cm = NPCScriptManager.getInstance().getCM(c);
    if (points == 50) {
      if (cm.canHold(1302000, 1)) {
        cm.sendNext("Amazing~ You solved every question. But, I'm sorry to tell you...there hasn't been a single trace of Master M. But since you worked so hard, I'll give you this.");
        c.getPlayer().gainMeso(100000000, true, true, true);
      } else {
        cm.sendNext("I'm really sorry that you do not have enough space to keep the reward...Oh well..Check your inventory next time and try again.");
      }
    } else if (points == 0) {
      cm.sendNext("Wow...You didn't obtained a point at all. Therefore, I can't give you any rewards.");
    } else {
      cm.sendNext("Well done, you've obtained " + points + " points out of " + INITIAL_QUESTION + " points. Here's some reward for you.");
      c.getPlayer().gainMeso(100 * points, true, true, true);
    }
    cm.dispose();
    c.getPlayer().setSpeedQuiz(null);
  }
}
