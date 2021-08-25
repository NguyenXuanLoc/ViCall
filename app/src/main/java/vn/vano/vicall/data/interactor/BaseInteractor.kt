package vn.vano.vicall.data.interactor

import vn.vano.vicall.data.ServiceFactory

abstract class BaseInteractor {
    protected val service by lazy { ServiceFactory.create() }
}