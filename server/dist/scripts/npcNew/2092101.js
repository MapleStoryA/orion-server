if (field.getMonsterCount() > 0) {
	self.sayOk("Hurry up, kill all the pirates!");
	return;
}
self.say("Thank you for rescuing me. Lets hurry and get back to the town.");
inventory.exchange(0, 4032497, 1);
field.clearEventInstance();
target.registerTransferField(251000000, "");
