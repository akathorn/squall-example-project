# Squall Example Project
This project shows an example on how to use [Squall](https://github.com/epfldata/squall). It isn't mean to showcase Squall's features, but instead to provide a set of steps that can help you to quickly start experimenting or to create a new project using it. This example is inspired in [Summing Bird's wordcount](https://github.com/twitter/summingbird#getting-started-word-count-with-twitter), and the code for obtaining tweets was mainly based on [this post](https://bcomposes.wordpress.com/2013/02/09/using-twitter4j-with-scala-to-access-streaming-tweets/).

For this tutorial, we are going to be running everything in local mode. Please refer to the [Squall documentation](https://github.com/epfldata/squall/wiki) for more details on how to run Squall in a cluster. 

Please, take the time to [report](https://github.com/akathorn/squall-example-project/issues/new) any problems you encounter.


To get started, clone this repository and `cd` to its directory:
```bash
  $ git clone https://github.com/akathorn/squall-example-project.git && cd squall-example-project
```

## Configuring your Twitter access keys
Before we start to have fun with Squall, it is necessary to set up your Twitter access keys in the [Utils.scala](https://github.com/akathorn/squall-example-project/blob/master/Util.scala) file. To obtain the keys, go to https://apps.twitter.com/ and create a new app. There is an explanation on how to to this in [the post](https://bcomposes.wordpress.com/2013/02/09/using-twitter4j-with-scala-to-access-streaming-tweets/) mentioned above, under the section called "Setting up authorization".

## Using the Squall REPL to run the example
If everything was set up correctly, we can get started with the real stuff. We are going to grab tweets around Switzerland using the Twitter API, and count the words in them using Squall's functional interface.

To start the [Squall interactive shell](https://github.com/epfldata/squall/wiki/Squall-REPL), run `sbt console` in the project directory. This might take a while the first time you run it, as it will fetch dependencies and compile everything. Once this is done, the REPL will preload Squall and we can then use the console to construct and run a Squall query plan.


First, we need to register a reader provider for Twitter. This reader provider was defined for this example [here](https://github.com/akathorn/squall-example-project/blob/master/TwitterStream.scala#L33).
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

Of course, your results will be different. You can try to wait a few minutes and see what you collected. Of course, Squall's REPL is also a Scala REPL and therefore you can do any operations you want with the resulting map.


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
