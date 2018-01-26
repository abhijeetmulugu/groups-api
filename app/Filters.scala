import javax.inject.Inject

import play.api.http.DefaultHttpFilters
import play.filters.cors.CORSFilter
import play.filters.csrf.CSRFFilter
import play.filters.hosts.AllowedHostsFilter

class Filters @Inject()(
                         csrfFilter: CSRFFilter,
                         allowedHostsFilter: AllowedHostsFilter,
                         corsFilter: CORSFilter
                       ) extends DefaultHttpFilters(
  csrfFilter,
  allowedHostsFilter,
  corsFilter
)