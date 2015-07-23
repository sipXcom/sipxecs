db = new Mongo().getDB("node");
// update registrations
db.registrar.find().forEach(function(doc) {
  db.registrar.update({"_id":doc._id},{$set:{"expirationTime":getDateFromEpoch(doc.expirationTime + "")}} );
});
// update subscriptions
db.subscription.find().forEach(function(doc) {  
  db.subscription.update({"_id":doc._id},{$set:{"expires":getDateFromEpoch(doc.expires)}} );     
});

function getDateFromEpoch(mEpoch) {
  var mEpochAsInt = parseInt(mEpoch);
  var expDate = new Date();
  if (mEpochAsInt<10000000000) mEpochAsInt *= 1000;
  expDate.setTime(mEpochAsInt);
  return expDate;
}