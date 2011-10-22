package cldellow.ballero.data

case class GoogleResponse(status: String, results: List[GoogleResult])
case class GoogleResult(types: List[String],
  formatted_address: String,
  address_components: List[GoogleAddressComponent],
  geometry: GoogleGeometry)
case class GoogleAddressComponent(types: List[String], long_name: String, short_name: String)
case class GoogleGeometry(location: GoogleLocation, location_type: String, viewport: GoogleViewport, bounds:
Option[GoogleViewport])
case class GoogleLocation(lat: BigDecimal, lng: BigDecimal)
case class GoogleViewport(northeast: GoogleLocation, southwest: GoogleLocation)



// vim: set ts=2 sw=2 et:
