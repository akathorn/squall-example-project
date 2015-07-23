package quickstart

import twitter4j._

object Util {
  val config = new twitter4j.conf.ConfigurationBuilder()
    .setOAuthConsumerKey("[your consumer key here]")
    .setOAuthConsumerSecret("[your consumer secret here]")
    .setOAuthAccessToken("[your access token here]")
    .setOAuthAccessTokenSecret("[your access token secret here]")
    .build
}
