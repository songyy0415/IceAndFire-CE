# EMI 迁移文档（MC 1.21.1 → 26.2）

> 对比：`D:/aiminecraftdev/emi1.21.1`（1.1.x）vs `D:/aiminecraftdev/emi26.2`（1.1.24）。
> EMI 26.2 为本地移植构建（`D:/aiminecraftdev/emi26.2` → mavenLocal）。

---

## 一、坐标

| 项 | 1.21.1 | 26.2 |
|---|---|---|
| 坐标 | `dev.emi:emi-fabric:1.1.19+1.21.1`（`:api` classifier） | `dev.emi:emi-fabric:1.1.24-3bfa16a4+26.2`（本地构建 → mavenLocal） |

## 二、API 对照（旧 → 新）

| 旧 API | 新 API | 判定 |
|---|---|---|
| `EmiRegistry.getRecipeManager().getAllRecipesFor(RecipeType)` | `EmiRegistry.getRecipeMap().byType(RecipeType)` → `Collection<RecipeHolder<T>>` | `getRecipeMap()` 返回 **MC 26.2 `RecipeMap`**（`net.minecraft.world.item.crafting.RecipeMap.byType`），替代旧 `RecipeManager.getAllRecipesFor` |
| `RecipeHolder.id()` → `Identifier` | `RecipeHolder.id()` → `ResourceKey<Recipe<?>>`（`RecipeHolder.java:8`）→ 需 `.location()` | MC `RecipeHolder` 变 record，`id` 从 `ResourceLocation` 变 `ResourceKey` |
| `EmiPlugin` / `EmiRegistry` / `EmiRecipe` / `EmiRecipeCategory` / `EmiIngredient` / `EmiStack` / `WidgetHolder` / `EmiTexture` / `@EmiEntrypoint` | 同 | **无变化**（EMI xplat API 稳定） |

## 三、修改文件

- `common/.../compat/emi/ForgeRecipeHolder.java`：
  - `registry.getRecipeManager().getAllRecipesFor(...)` → `registry.getRecipeMap().byType(...)`（`List`→`new ArrayList<>(...)`）
  - `this.entry.id()` → `this.entry.id().location()`（`ResourceKey.location()` 返回 `Identifier`）
  - 新增 `import java.util.ArrayList;`
- `common/.../compat/emi/IceAndFireEmiPlugin.java`：**无改动**（`@EmiEntrypoint` + `EmiPlugin.register` 26.2 不变）
- `fabric.mod.json` 的 `"emi"` entrypoint：不变

## 四、行为变化

- **无**。Dragon Forge 三系（火/冰/雷）配方仍按配方类型显示在 EMI 分类中。

## 五、验证

- `./gradlew :common:compileJava`：错误 2,179 → 2,178（EMI 相关 0 错误）。
