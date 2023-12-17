function onEventStart() {
   event.log("testing")
   event.registerTransferField(10000000);
   event.schedule("onFinish", 10)
}

function onFinish() {
   event.finish();
}