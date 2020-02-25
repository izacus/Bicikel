package si.virag.bicikelj

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component
@Singleton
interface BicikeljComponent {
    fun inject(activity: MainActivity)

    fun context() : Context

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): BicikeljComponent
    }
}