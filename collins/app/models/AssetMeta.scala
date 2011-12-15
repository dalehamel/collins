package models

import anorm._
import anorm.defaults._
import play.api.cache.Cache
import play.api.Play.current
import java.sql._

case class AssetMeta(
    id: Pk[java.lang.Long],
    name: String,
    priority: Int,
    label: String,
    description: String)
{
  def getId(): Long = id.get
}

object AssetMeta extends Magic[AssetMeta](Some("asset_meta")) with Dao[AssetMeta] {

  def create(metas: Seq[AssetMeta])(implicit con: Connection): Seq[AssetMeta] = {
    metas.foldLeft(List[AssetMeta]()) { case(list, meta) =>
      if (meta.id.isDefined) throw new IllegalArgumentException("Use update, id already defined")
      AssetMeta.create(meta) +: list
    }.reverse
  }

  def findById(id: Long) = Model.withConnection { implicit con =>
    AssetMeta.find("id={id}").on('id -> id).singleOption()
  }

  def getViewable(): Seq[AssetMeta] = {
    // change to use stuff in Enum
    Model.withConnection { implicit connection =>
      Cache.get[List[AssetMeta]]("AssetMeta.getViewable").getOrElse {
        logger.debug("Cache miss for AssetMeta.getViewable")
        val res = AssetMeta.find("priority > -1 order by priority asc").list()
        Cache.set("AssetMeta.getViewable", res)
        res
      }
    }
  }

  type Enum = Enum.Value
  object Enum extends Enumeration(1) {
    val ServiceTag = Value(1, "SERVICE_TAG")
    val ChassisTag = Value(2, "CHASSIS_TAG")
    val RackPosition = Value(3, "RACK_POSITION")
    val PowerPort = Value(4, "POWER_PORT")
    val SwitchPort = Value(5, "SWITCH_PORT")

    val CpuCount = Value(6, "CPU_COUNT")
    val CpuCores = Value(7, "CPU_CORES")
    val CpuThreads = Value(8, "CPU_THREADS")
    val CpuSpeedGhz = Value(9, "CPU_SPEED_GHZ")
    val CpuDescription = Value(10, "CPU_DESCRIPTION")

    val MemoryAvailableBytes = Value(11, "MEMORY_SIZE_BYTES")
    val MemoryBanksUsed = Value(12, "MEMORY_BANKS_USED")
    val MemoryBanksUnused = Value(13, "MEMORY_BANKS_UNUSED")
    val MemoryDescription = Value(14, "MEMORY_DESCRIPTION")

    val NicSpeed = Value(15, "NIC_SPEED") // in bits
    val MacAddress = Value(16, "MAC_ADDRESS")
    val NicDescription = Value(17, "NIC_DESCRIPTION")

    val DiskSizeBytes = Value(18, "DISK_SIZE_BYTES")
    val DiskType = Value(19, "DISK_TYPE")
    val DiskDescription = Value(20, "DISK_DESCRIPTION")
    val DiskIsFlash = Value(21, "DISK_IS_FLASH")
    val DiskStorageTotal = Value(22, "DISK_STORAGE_TOTAL")
  }
}


