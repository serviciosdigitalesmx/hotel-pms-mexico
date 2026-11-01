using FluentAssertions;
using RepairShop.Application.Billing;
using RepairShop.Domain.Billing;

namespace RepairShop.Application.Tests;

public sealed class SubscriptionBillingRoutingTests
{
    [Theory]
    [InlineData("AR", "ARS", BillingProvider.MercadoPago)]
    [InlineData("MX", "MXN", BillingProvider.MercadoPago)]
    [InlineData("US", "USD", BillingProvider.Paddle)]
    [InlineData("ES", "USD", BillingProvider.Paddle)]
    public void Country_selects_expected_subscription_currency_and_gateway(string country, string currency, BillingProvider provider)
    {
        SubscriptionService.CurrencyFor(country).Should().Be(currency);
        SubscriptionService.ProviderFor(country).Should().Be(provider);
    }

    [Fact]
    public void Mexican_plan_prices_use_the_approved_amounts_and_reject_unknown_currency()
    {
        var options = new BillingOptions();

        options.PriceFor(PlanId.Basic, "MXN").Should().Be(300m);
        options.PriceFor(PlanId.Standard, "MXN").Should().Be(450m);
        options.PriceFor(PlanId.Pro, "MXN").Should().Be(600m);
        options.PriceFor(PlanId.Basic, "CAD").Should().BeNull();
    }
}
