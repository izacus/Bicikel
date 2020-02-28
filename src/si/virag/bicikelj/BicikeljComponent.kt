package si.virag.bicikelj

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import si.virag.bicikelj.station_map.StationMapFragment
import si.virag.bicikelj.stations.StationListFragment
import javax.inject.Singleton

@Component
@Singleton
interface BicikeljComponent {
    fun inject(activity: MainActivity)
    fun inject(fragment: StationMapFragment)
    fun inject(fragment: StationListFragment)

    fun context() : Context

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): BicikeljComponent
    }
}