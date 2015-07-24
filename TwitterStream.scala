package quickstart

import ch.epfl.data.squall.utilities.CustomReader
import ch.epfl.data.squall.utilities.SquallContext
import ch.epfl.data.squall.utilities.ReaderProvider
import ch.epfl.data.squall.api.scala.Stream._
import twitter4j._
import java.util.concurrent.LinkedBlockingQueue

class StatusStreamer(twitterStream: TwitterStream) extends CustomReader {
  // Initialization
  val queue = new LinkedBlockingQueue[String](1000)
  val area = Array(Array(5.9517912865,45.9720796059),Array(10.4178924561,47.634536498)) // Switzerland
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
