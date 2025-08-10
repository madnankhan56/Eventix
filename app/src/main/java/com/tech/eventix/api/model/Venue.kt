data class NetworkVenue(
    val name: String,
    val city: City,
    val state: State,
    val address: Address,
    val postalCode: String,
    val location: Location,
    val images: List<Image>,
    val url: String,
    val locale: String,
    val boxOfficeInfo: BoxOfficeInfo?,
    val parkingDetail: String?,
    val generalInfo: GeneralInfo?
)

data class BoxOfficeInfo(
    val openHoursDetail: String?
)

data class GeneralInfo(
    val generalRule: String?,
    val childRule: String?
)

data class City(
    val name: String
)