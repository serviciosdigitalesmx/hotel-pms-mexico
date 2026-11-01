using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.EntityFrameworkCore.Migrations;
using RepairShop.Infrastructure.Persistence;

#nullable disable

namespace RepairShop.Infrastructure.Persistence.Migrations;

/// <inheritdoc />
[DbContext(typeof(RepairShopDbContext))]
[Migration("20261101090000_ExplicitReceptionChecklist")]
public partial class ExplicitReceptionChecklist : Migration
{
    /// <inheritdoc />
    protected override void Up(MigrationBuilder migrationBuilder)
    {
        migrationBuilder.AlterColumn<bool>(name: "ScreenOk", table: "order_reception_checklists", type: "boolean", nullable: true, oldClrType: typeof(bool), oldType: "boolean");
        migrationBuilder.AlterColumn<bool>(name: "CamerasOk", table: "order_reception_checklists", type: "boolean", nullable: true, oldClrType: typeof(bool), oldType: "boolean");
        migrationBuilder.AlterColumn<bool>(name: "SpeakersOk", table: "order_reception_checklists", type: "boolean", nullable: true, oldClrType: typeof(bool), oldType: "boolean");
        migrationBuilder.AlterColumn<bool>(name: "MicrophoneOk", table: "order_reception_checklists", type: "boolean", nullable: true, oldClrType: typeof(bool), oldType: "boolean");
        migrationBuilder.AlterColumn<bool>(name: "ButtonsOk", table: "order_reception_checklists", type: "boolean", nullable: true, oldClrType: typeof(bool), oldType: "boolean");
        migrationBuilder.AlterColumn<bool>(name: "FaceIdOk", table: "order_reception_checklists", type: "boolean", nullable: true, oldClrType: typeof(bool), oldType: "boolean");
        migrationBuilder.AlterColumn<bool>(name: "FingerprintOk", table: "order_reception_checklists", type: "boolean", nullable: true, oldClrType: typeof(bool), oldType: "boolean");
        migrationBuilder.AddColumn<bool>(name: "PowersOn", table: "order_reception_checklists", type: "boolean", nullable: true);
        migrationBuilder.AddColumn<bool>(name: "ChargingOk", table: "order_reception_checklists", type: "boolean", nullable: true);
    }

    /// <inheritdoc />
    protected override void Down(MigrationBuilder migrationBuilder)
    {
        migrationBuilder.DropColumn(name: "PowersOn", table: "order_reception_checklists");
        migrationBuilder.DropColumn(name: "ChargingOk", table: "order_reception_checklists");
        migrationBuilder.AlterColumn<bool>(name: "ScreenOk", table: "order_reception_checklists", type: "boolean", nullable: false, defaultValue: false, oldClrType: typeof(bool), oldType: "boolean", oldNullable: true);
        migrationBuilder.AlterColumn<bool>(name: "CamerasOk", table: "order_reception_checklists", type: "boolean", nullable: false, defaultValue: false, oldClrType: typeof(bool), oldType: "boolean", oldNullable: true);
        migrationBuilder.AlterColumn<bool>(name: "SpeakersOk", table: "order_reception_checklists", type: "boolean", nullable: false, defaultValue: false, oldClrType: typeof(bool), oldType: "boolean", oldNullable: true);
        migrationBuilder.AlterColumn<bool>(name: "MicrophoneOk", table: "order_reception_checklists", type: "boolean", nullable: false, defaultValue: false, oldClrType: typeof(bool), oldType: "boolean", oldNullable: true);
        migrationBuilder.AlterColumn<bool>(name: "ButtonsOk", table: "order_reception_checklists", type: "boolean", nullable: false, defaultValue: false, oldClrType: typeof(bool), oldType: "boolean", oldNullable: true);
        migrationBuilder.AlterColumn<bool>(name: "FaceIdOk", table: "order_reception_checklists", type: "boolean", nullable: false, defaultValue: false, oldClrType: typeof(bool), oldType: "boolean", oldNullable: true);
        migrationBuilder.AlterColumn<bool>(name: "FingerprintOk", table: "order_reception_checklists", type: "boolean", nullable: false, defaultValue: false, oldClrType: typeof(bool), oldType: "boolean", oldNullable: true);
    }
}
