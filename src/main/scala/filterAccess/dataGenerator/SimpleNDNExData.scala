package filterAccess.dataGenerator

import filterAccess.json._
import filterAccess.crypto.Helpers.computeHash


/**
 * Created by Claudio Marxer <marxer@claudio.li>
 *
 * Sample data generator for SimpleNDNExSetup (and possibly other runnables)
 *
 * Content Channel
 * Track - 6 Track Points
 *
 * Key Channel
 * Keys - Permission Level 0 with Key 99, Permission Level 1 with Key 44
 *
 * Permission Channel
 * Permissions - 3 Users (user1 on level 0, user2 on level 1, user3 on level 0)
 *
 */
object SimpleNDNExData {

  // -----------------------------------------------------------------------------
  // ==== CONTENT CHANNEL ========================================================
  // -----------------------------------------------------------------------------

  /**
   * Generate a Track
   * @param name Content Object Name (added to JSON Object)
   * @param i Parameter to influence coordinates of track (deterministic, different i generate different tracks)
   * @return Track as String (JSON Object)
   */
  def generateTrackJSON(name: String, i: Int = 0): String = {
    ContentChannelBuilder.buildTrack(
      List(
        TrackPoint(3 + i, 4 + i * 2, 6 + i),
        TrackPoint(4 + i, 4 + i, 6 + i * 3),
        TrackPoint(4 + i * 2, 5 + i, 6 + i),
        TrackPoint(4 + i, 6 + i * 4, 7 + i),
        TrackPoint(6 + i * 2, 6 + i, 5 + i * 2),
        TrackPoint(5 + i, 6 + i, 5 + i * 3)
      ),
      name
    )
  }

  /**
   * Same data as generateTrackJSON but in Array[Byte]
   * @param name Content Object Name (added to JSON Object)
   * @param i Parameter to influence coordinates of track (deterministic, different i generate different tracks)
   * @return Track as Array[Byte]
   */
  def generateTrack(name: String, i: Int = 0): Array[Byte] = generateTrackJSON(name, i).getBytes


  // -----------------------------------------------------------------------------
  // ==== KEY CHANNEL ============================================================
  // -----------------------------------------------------------------------------


  /**
   * Generate symmetric keys to secure certain data
   *
   * @param    name     Content Object Name (added to JSON Object)
   * @param    i        Parameter to influence keys (deterministic, different i generate different tracks)
   * @return            Keys as String (JSON Object)
   */
  def generateKeysJSON(name: String, i: Int): String = {
    KeyChannelBuilder.buildKeys(
      Map(
        AccessLevel(0) -> LevelKey( computeHash((99111111*i).toString) ), // Permission to access permission data
        AccessLevel(1) -> LevelKey( computeHash((99222222*i).toString) ),
        AccessLevel(2) -> LevelKey( computeHash((99333333*i).toString) )
      ),
      name
    )
  }

  /**
   * Returns same data as generateKeysJSON but in Array[Byte]
   * @param name Content Object Name (added to JSON Object)
   * @return Keys as Array[Byte]
   */
  def generateKeys(name: String, i: Int): Array[Byte] = generateKeysJSON(name, i).getBytes


  // -----------------------------------------------------------------------------
  // ==== PERMISSION CHANNEL =====================================================
  // -----------------------------------------------------------------------------

  /**
   * Generate Permissions
   * @param name Content Object Name (added to JSON Object)
   * @return Permissions as String (JSON Object)
   */
  def generatePermissionsJSON(name: String): String = {
    AccessChannelBuilder.buildPermissions(
      List(
        UserLevel("18387373", 1),  // access unfiltered data
        UserLevel("237494854", 0), // access permission data
        UserLevel("237494854", 1), // access unfiltered data
        UserLevel("237494854", 2), // access data on first filter level
        UserLevel("348474", 1)     // access unfiltered data
      ),
      name
    )
  }

  /**
   * Same data as generateKPermissionsJSON but in Array[Byte]
   * @param name Content Object Name (added to JSON Object)
   * @return Permissions as Array[Byte]
   */
  def generatePermissions(name: String): Array[Byte] = generatePermissionsJSON(name).getBytes

}
