package vn.vano.vicall.ui.home

import android.Manifest
import android.content.pm.PackageManager
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_home.*
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.ui.base.BaseFragment

class HomeFragment : BaseFragment<HomeView, HomePresenterImp>(), HomeView {

    companion object {
        private const val RC_PERMISSION_READ_CALL_LOG = 1

        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_READ_CALL_LOG && grantResults.isNotEmpty()) {
            var granted = grantResults.isNotEmpty()
            for (p in grantResults) {
                if (p != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }

            if (granted) {
                checkCallLogPermission()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun initView(): HomeView {
        return this
    }

    override fun initPresenter(): HomePresenterImp {
        return HomePresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun initWidgets() {
        // Check permission first
        checkCallLogPermission()

        // Listeners
        lblGrant.setOnSafeClickListener {
            checkCallLogPermission(true)
        }
    }

    private fun initPagerAdapter() {
        pagerHome.adapter = HomeStateAdapter(parentActivity)
        pagerHome.offscreenPageLimit = pagerHome.adapter?.itemCount ?: 1
        pagerHome.isUserInputEnabled = false

        TabLayoutMediator(tabsHome, pagerHome) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.call_history)
                1 -> getString(R.string.spam_call)
                2 -> getString(R.string.blocked_call)
                else -> ""
            }
        }.attach()
    }

    private fun checkCallLogPermission(requestPermission: Boolean = false) {
        if (PermissionUtil.isGranted(
                self,
                arrayOf(Manifest.permission.READ_CALL_LOG),
                RC_PERMISSION_READ_CALL_LOG,
                requestPermission
            )
        ) {
            // Init pager adapter
            tabsHome.visible()
            pagerHome.visible()
            initPagerAdapter()

            // Hide permission layout
            llPermission.gone()
        } else {
            // Show permission layout
            llPermission.visible()

            // Hide pager layout
            tabsHome.gone()
            pagerHome.gone()
        }
    }
}
