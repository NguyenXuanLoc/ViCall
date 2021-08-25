package vn.vano.vicall.common.ext

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.jetbrains.anko.ctx
import vn.vano.vicall.common.util.PermissionUtil

fun AppCompatActivity.addFragment(containerId: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction().add(containerId, fragment, fragment.TAG).commit()
}

fun AppCompatActivity.replaceFragment(containerId: Int, fragment: Fragment) {
    supportFragmentManager.beginTransaction().replace(containerId, fragment, fragment.TAG).commit()
}

fun AppCompatActivity.openActivity(
    clz: Class<*>, bundle: Bundle? = null, clearStack: Boolean = false,
    enterAnim: Int? = null, exitAnim: Int? = null, flags: IntArray? = null
) {
    val intent = Intent(ctx, clz)
    if (flags?.isNotEmpty() == true) {
        for (flag in flags) {
            intent.addFlags(flag)
        }
    }
    if (clearStack) {
        setResult(Activity.RESULT_CANCELED)
        finishAffinity()
    }
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
    enterAnim?.also { enter ->
        exitAnim?.also { exit ->
            overridePendingTransition(enter, exit)
        }
    }
}

fun AppCompatActivity.openActivityForResult(
    clz: Class<*>,
    requestCode: Int,
    bundle: Bundle? = null,
    enterAnim: Int? = null,
    exitAnim: Int? = null
) {
    val intent = Intent(ctx, clz)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivityForResult(intent, requestCode)
    enterAnim?.also { enter ->
        exitAnim?.also { exit ->
            overridePendingTransition(enter, exit)
        }
    }
}

fun AppCompatActivity.closeActivity(enterAnim: Int? = null, exitAnim: Int? = null) {
    finishAfterTransition()
    enterAnim?.also { enter ->
        exitAnim?.also { exit ->
            overridePendingTransition(enter, exit)
        }
    }
}

fun AppCompatActivity.closeActivityAndClearStack() {
    setResult(Activity.RESULT_CANCELED)
    finishAffinity()
}

fun AppCompatActivity.requestCallScreeningRole(requestCode: Int) {
    if (PermissionUtil.isApi29orHigher()) {
        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        startActivityForResult(intent, requestCode)
    }
}

fun AppCompatActivity.requestCallRedirectionRole(requestCode: Int) {
    if (PermissionUtil.isApi29orHigher()) {
        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_REDIRECTION)
        startActivityForResult(intent, requestCode)
    }
}