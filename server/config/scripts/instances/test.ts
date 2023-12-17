function onEventStart() {
   event.log("testing")
   event.schedule("onFinish", 10)
}

function onFinish() {
   event.finish();
}