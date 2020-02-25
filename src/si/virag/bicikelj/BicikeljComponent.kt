package si.virag.bicikelj

import dagger.Component

@Component
interface BicikeljComponent {
    fun inject(activity: MainActivity)
}