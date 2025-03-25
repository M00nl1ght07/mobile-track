package com.meter_alc_rgb.gpstrecker.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meter_alc_rgb.gpstrecker.R
import com.meter_alc_rgb.gpstrecker.databinding.TrackItemBinding

/**
 * Адаптер для отображения списка маршрутов в RecyclerView.
 * Использует ListAdapter для эффективного обновления данных с помощью DiffUtil.
 * 
 * @param listener Слушатель для обработки нажатий на элементы списка
 */
class TrackAdapter(private val listener: Listener) : ListAdapter<TrackItem, TrackAdapter.TrackHolder>(Comparator()) {

    /**
     * Создает новый ViewHolder для элемента списка.
     * 
     * @param parent Родительская ViewGroup
     * @param viewType Тип представления
     * @return Новый экземпляр TrackHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)
        return TrackHolder(view, listener)
    }

    /**
     * Связывает данные с ViewHolder.
     * 
     * @param holder ViewHolder для заполнения данными
     * @param position Позиция элемента в списке
     */
    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
         holder.bind(getItem(position))
    }

    /**
     * ViewHolder для хранения и управления представлением элемента списка.
     * 
     * @param view Представление элемента списка
     * @param listener Слушатель для обработки нажатий
     */
    class TrackHolder(view: View, private val listener: Listener) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private val binding = TrackItemBinding.bind(view)
        private var item: TrackItem? = null
        
        init {
            itemView.setOnClickListener(this)
            binding.ibDelete.setOnClickListener(this)
        }
        
        /**
         * Заполняет представление данными маршрута.
         * 
         * @param track Объект маршрута для отображения
         */
        fun bind(track: TrackItem) = with(binding) {
            item = track
            val time = "Время: ${track.time} м"
            val velocity = "Расстояние: ${track.velocity} км / ч"
            val distance = "${track.distance} км"
            tvDate.text = track.date
            tvTrackTime.text = time
            tvDistance.text = distance
            tvAverageVelocity.text = velocity
        }

        /**
         * Обрабатывает нажатия на элементы представления.
         * 
         * @param v Представление, на которое было совершено нажатие
         */
        override fun onClick(v: View) {
            if(v.id == R.id.ibDelete){
                item?.let { listener.onClick(it, ClickType.DELETE) }
            } else {
                item?.let { listener.onClick(it, ClickType.OPEN) }
            }
        }
    }

    /**
     * Класс для сравнения элементов списка при обновлении.
     * Используется DiffUtil для эффективного обновления только изменившихся элементов.
     */
    class Comparator : DiffUtil.ItemCallback<TrackItem>(){
        /**
         * Проверяет, представляют ли два элемента один и тот же объект.
         * 
         * @return true, если элементы имеют одинаковый id
         */
        override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Проверяет, имеют ли два элемента одинаковое содержимое.
         * 
         * @return true, если элементы полностью идентичны
         */
        override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Интерфейс для обработки нажатий на элементы списка.
     */
    interface Listener{
        /**
         * Вызывается при нажатии на элемент списка.
         * 
         * @param item Объект маршрута, на который было совершено нажатие
         * @param type Тип нажатия (удаление или открытие)
         */
        fun onClick(item: TrackItem, type: ClickType)
    }

    /**
     * Перечисление типов нажатий на элементы списка.
     */
    enum class ClickType{
        DELETE,  // Удаление маршрута
        OPEN     // Открытие маршрута для просмотра
    }
}