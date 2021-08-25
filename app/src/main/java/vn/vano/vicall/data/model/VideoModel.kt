package vn.vano.vicall.data.model

data class VideoModel(val fileId: String?) : BaseModel() {
    var downloadCount: Int = 0
    var viewCount: Int = 0
    var fileName: String? = null
    var thumbnail: String? = null
    var price: Int = 0
    var duration: Long = 0
    var isApplied: Boolean = false
    var isApproved: Boolean = false
    var isWaitingApproved: Boolean = false
    var isMyVideo: Boolean = false
}