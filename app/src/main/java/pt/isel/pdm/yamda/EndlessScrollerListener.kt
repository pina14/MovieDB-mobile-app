package pt.isel.pdm.yamda

import android.widget.AbsListView

abstract class EndlessScrollerListener(private val totalPages: Int) : AbsListView.OnScrollListener{

    private @Volatile var visibleThreshold = 5

    private @Volatile var currentPage = 1

    private @Volatile var previousTotalItemCount = 0

    private @Volatile var loading = false

    private @Volatile var startingPageIndex = 0


    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex
            this.previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) {
                this.loading = true
            }
        }
        if (loading && totalItemCount > previousTotalItemCount && currentPage + 1 <= totalPages - 1) {
            loading = false
            previousTotalItemCount = totalItemCount
            currentPage++
        }

        if (!loading && firstVisibleItem + visibleItemCount + visibleThreshold >= totalItemCount) {
            loading = onLoadMore(currentPage + 1, totalItemCount)
        }
    }

    abstract fun onLoadMore(page: Int, totalItemsCount: Int): Boolean

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {} // DO NOTHING

}