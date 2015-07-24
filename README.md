# Squall Example Project
This project shows an example on how to use [Squall](https://github.com/epfldata/squall).

This readme is under construction ;)

## Using the Squall REPL to run the example
To start the Squall interactive shell, run `sbt console` in the project directory.

First, we need to register the reader provider we defined for Twitter.
```Scala
scala> context.registerReaderProvider(new TwitterProvider())
```

Now we can define a `Source` of strings coming from Twitter
```Scala
scala> val tweets = Source[String]("twitter")
```

We can use the flatMap operator to split the string into words
```Scala
scala> val words = tweets flatMap { t: String => t.split(" ") }
```

And count the number of appearances for each word by grouping them
```Scala
scala> val count = words groupByKey ( t => 1, t => t )
```

Finally, we can submit the plan through the Squall context

```Scala
scala> val result = context.submitLocal("wordcount", plan)
```

The variable result is a map that will be continously updated as we receive tweets. After waiting for a while we can try to see what we have gathered:


```Scala
scala> result
res2: java.util.Map[String,String] =
{give=1, @mariacunninghmd=1, @SEOZib=1, in=2, FOR=1, unsere=1, agario=1, summer=1, #Controleurs=1, @LindsaySkarda=1, STARFOULLAH=1, @FSGeneva=1, ONT=1, Literally=1, bal=1, experts=1, BEHIND=1, ptn=1, it...=1, BAR.
NO=1, #vacation=1, m'a=1, Wochenende=1, life....=1, lmao=1, DES=1, alle!=1, opens=1, @zephoria=1, delightful=1, up=1, ILS=1, Meer=1, a=1, für=1, fab=1, CHOORON.=1,
試しに投げてみるやで=1, wish=1, http://t.co/WklxWUcBAi=1, must=1, (via=1, Schönes=1, 🇦🇹=1, New=1, tt=1, https://t.co/pcfjrM7RnI=1, PTDDDDDDDDDDDDDR=1, CABINET=1, am=1, @DhariLo=1, the=1, http://t.co/5ZBY0G86U8=1, weekend!=1, enculée=1, of=1, Austria=1, Ostsee-Wellnesshotels:=1, @Netzoekonom=1, downloading=1, #MENA=1, hors=1, @yoichi_nko=1, チューリッヒめっちゃチャリ勢いる=1, et=1, @mojitohowell=1, -=1, ...
scala> result.get("summer")
res9: String = 1
```




## Step by step how-to
### Setting up the SBT dependencies
### Defining a new data source
Using Twitter Streaming API to obtain tweets.

[TwitterStream.scala](./TwitterStream.scala)
```Scala
import ch.epfl.data.squall.utilities.{CustomReader, SquallContext, ReaderProvider}
import twitter4j._
import java.util.concurrent.LinkedBlockingQueue

class StatusStreamer(twitterStream: TwitterStream) extends CustomReader {
  // Initialization
  val queue = new LinkedBlockingQueue[String](1000)
  val area = Array(Array(5.9517912865,45.9720796059),
                   Array(10.4178924561,47.634536498)) // Switzerland
  twitterStream.addListener(statusListener)
  twitterStream.filter(new FilterQuery().locations(area))

  def statusListener = new StatusListener() {
    def onStatus(status: Status) { queue.offer(status.getText) }
    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
    def onException(ex: Exception) { ex.printStackTrace }
    def onScrubGeo(arg0: Long, arg1: Long) {}
    def onStallWarning(warning: StallWarning) {}
  }

  override def readLine(): String = {
    queue.take()
  }

  // Cleanup
  override def close() {
    twitterStream.cleanUp
    twitterStream.shutdown
  }
}

class TwitterProvider extends ReaderProvider {
  override def canProvide (context: SquallContext, name: String) = {
    name == "twitter"
  }

  override def getReaderForName (name: String, fileSection: Int, fileParts: Int): CustomReader = {
    if (name == "twitter") {
      val twitterStream = new TwitterStreamFactory(Util.config).getInstance
      new StatusStreamer(twitterStream)
    } else {
      null
    }
  }

  override def toString(): String = "[Twitter status provider]"
}
```
