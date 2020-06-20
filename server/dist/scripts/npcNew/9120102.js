teye = 0;
if (target.nGender() == 0) {
	changeFace1 = 20002 + teye;
	changeFace2 = 20003 + teye;
	changeFace3 = 20004 + teye;
	changeFace4 = 20007 + teye;
	changeFace5 = 20008 + teye;
	changeFace6 = 20009 + teye;
	changeFace7 = 20010 + teye;
	changeFace8 = 20011 + teye;
	changeFace9 = 20013 + teye;
	changeFace10 = 20016 + teye;
	changeFace11 = 20017 + teye;
	mFace = self.askAvatar(
					"With the magic of plastic surgery, I can totally"
							+ " transform your face into something new. Don't you want to sport a beautiful,"
							+ " healthy face, complete with the admirations of those around you?"
							+ " For #b#t5152009##k, I'll make that come true."
							+ " Choose the face of your liking.", 5152009,
					changeFace1, changeFace2, changeFace3, changeFace4,
					changeFace5, changeFace6, changeFace7, changeFace8,
					changeFace9, changeFace10, changeFace11);

} else if (target.nGender() == 1) {
	changeFace1 = 21003 + teye;
	changeFace2 = 21003 + teye;
	changeFace3 = 21004 + teye;
	changeFace4 = 21005 + teye;
	changeFace5 = 21008 + teye;
	changeFace6 = 21009 + teye;
	changeFace7 = 21010 + teye;
	changeFace8 = 21011 + teye;
	changeFace9 = 21013 + teye;
	changeFace10 = 21016 + teye;
	changeFace11 = 21017 + teye;
	mFace = self.askAvatar(
					"With the magic of plastic surgery, I can totally transform your face into something new. Don't you want to sport a beautiful, healthy face, complete with the admirations of those around you? For #b#t5152009##k, I'll make that come true. Choose the face of your liking.",
					5152009, changeFace1, changeFace2, changeFace3,
					changeFace4, changeFace5, changeFace6, changeFace7,
					changeFace8, changeFace9, changeFace10, changeFace11);
	
}
if (mFace == 1)
	self.say("Alright, the procedure's done. Check it out, here's the mirror for you. What do you think? Even I admit this looks like a masterpiece. Now, once you get sick of that new look, please drop by, alright? ");
else if (mFace == -1)
	self.say("I'm afraid you don't have our designated plastic surgery coupon. I'm sorry, but without the coupon, there's no plastic surgery for you.");
else if (mFace == -3)
	self.say("I'm afraid we have a problem here at the hospital and I can't go on with the procedure right this minute. Please come back later.");
else if (mFace == 0 || mFace == -2)
	self.say("Sorry to say this, but there seems to be a problem here with the procedure. Please come back later.");