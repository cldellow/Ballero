package cldellow.ballero.data

case class GoogleResponse(
  results: List[GoogleResult],
  status: String
)

case class GoogleResult(
  address_components: List[GoogleAddressComponent],
  formatted_address: String,
  geometry: GoogleGeometry,
  types: List[String]
)
case class GoogleAddressComponent(
  long_name: String,
  short_name: String,
  types: List[String]
)
case class GoogleGeometry(
  bounds: Option[GoogleViewport],
  location: GoogleLocation,
  location_type: String,
  viewport: GoogleViewport
)

case class GoogleLocation(
  lat: BigDecimal,
  lng: BigDecimal
)

case class GoogleViewport(
  northeast: GoogleLocation,
  southwest: GoogleLocation
)



// vim: set ts=2 sw=2 et:
