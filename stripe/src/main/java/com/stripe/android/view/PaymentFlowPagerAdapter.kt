package com.stripe.android.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.stripe.android.PaymentSessionConfig
import com.stripe.android.databinding.ShippingInfoPageBinding
import com.stripe.android.databinding.ShippingMethodPageBinding
import com.stripe.android.model.ShippingInformation
import com.stripe.android.model.ShippingMethod

internal class PaymentFlowPagerAdapter(
    private val context: Context,
    private val paymentSessionConfig: PaymentSessionConfig,
    private val allowedShippingCountryCodes: Set<String> = emptySet(),
    private val onShippingMethodSelectedCallback: (ShippingMethod) -> Unit = {}
) : PagerAdapter() {
    private val pages: List<PaymentFlowPage>
        get() {
            return listOfNotNull(
                PaymentFlowPage.ShippingInfo.takeIf {
                    paymentSessionConfig.isShippingInfoRequired
                },
                PaymentFlowPage.ShippingMethod.takeIf {
                    paymentSessionConfig.isShippingMethodRequired &&
                        (!paymentSessionConfig.isShippingInfoRequired || isShippingInfoSubmitted)
                }
            )
        }

    internal var shippingInformation: ShippingInformation? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    internal var isShippingInfoSubmitted: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    internal var shippingMethods: List<ShippingMethod> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    internal var selectedShippingMethod: ShippingMethod? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val viewHolder = when (pages[position]) {
            PaymentFlowPage.ShippingInfo -> {
                PaymentFlowViewHolder.ShippingInformationViewHolder(collection)
            }
            PaymentFlowPage.ShippingMethod -> {
                PaymentFlowViewHolder.ShippingMethodViewHolder(collection)
            }
        }
        when (viewHolder) {
            is PaymentFlowViewHolder.ShippingInformationViewHolder -> {
                viewHolder.bind(
                    paymentSessionConfig, shippingInformation, allowedShippingCountryCodes
                )
            }
            is PaymentFlowViewHolder.ShippingMethodViewHolder -> {
                viewHolder.bind(
                    shippingMethods,
                    selectedShippingMethod,
                    onShippingMethodSelectedCallback
                )
            }
        }
        collection.addView(viewHolder.itemView)
        return viewHolder.itemView
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return pages.size
    }

    internal fun getPageAt(position: Int): PaymentFlowPage? {
        return pages.getOrNull(position)
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view === o
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(pages[position].titleResId)
    }

    internal sealed class PaymentFlowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class ShippingInformationViewHolder(
            viewBinding: ShippingInfoPageBinding
        ) : PaymentFlowViewHolder(viewBinding.root) {
            constructor(
                root: ViewGroup
            ) : this(
                ShippingInfoPageBinding.inflate(
                    LayoutInflater.from(root.context),
                    root,
                    false
                )
            )

            private val shippingInfoWidget = viewBinding.shippingInfoWidget

            fun bind(
                paymentSessionConfig: PaymentSessionConfig,
                shippingInformation: ShippingInformation?,
                allowedShippingCountryCodes: Set<String>
            ) {
                shippingInfoWidget
                    .setHiddenFields(paymentSessionConfig.hiddenShippingInfoFields)
                shippingInfoWidget
                    .setOptionalFields(paymentSessionConfig.optionalShippingInfoFields)
                shippingInfoWidget
                    .setAllowedCountryCodes(allowedShippingCountryCodes)
                shippingInfoWidget
                    .populateShippingInfo(shippingInformation)
            }
        }

        class ShippingMethodViewHolder(
            viewBinding: ShippingMethodPageBinding
        ) : PaymentFlowViewHolder(viewBinding.root) {
            constructor(
                root: ViewGroup
            ) : this(
                ShippingMethodPageBinding.inflate(
                    LayoutInflater.from(root.context),
                    root,
                    false
                )
            )

            private val shippingMethodWidget =
                viewBinding.selectShippingMethodWidget

            fun bind(
                shippingMethods: List<ShippingMethod>,
                selectedShippingMethod: ShippingMethod?,
                onShippingMethodSelectedCallback: (ShippingMethod) -> Unit
            ) {
                shippingMethodWidget.setShippingMethods(shippingMethods)
                shippingMethodWidget.setShippingMethodSelectedCallback(
                    onShippingMethodSelectedCallback
                )
                selectedShippingMethod?.let {
                    shippingMethodWidget.setSelectedShippingMethod(it)
                }
            }
        }
    }
}
