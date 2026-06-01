import json, random, string

def uid():
    return ''.join(random.choices(string.ascii_letters + string.digits, k=8))

OUTPUT = "C:/Users/Anderson Ramos/backend-serveflow/serveflow-architecture.excalidraw"

# ═══════════════════════════════════════════════════════════════════════════════
# COLORS
# ═══════════════════════════════════════════════════════════════════════════════
C_BG       = "#1a1d2e"

# Avatares de papéis
C_ADMIN_S  = "#ef4444"; C_ADMIN_B  = "#3a0d0d"
C_GER_S    = "#f97316"; C_GER_B    = "#3a1d05"
C_COZ_S    = "#3b82f6"; C_COZ_B    = "#0d1f3c"
C_GAR_S    = "#22c55e"; C_GAR_B    = "#0a2218"
C_CXA_S    = "#a855f7"; C_CXA_B    = "#1c0a30"

# Hosts / Plataformas
C_FE_S     = "#e2e8f0"; C_FE_B     = "#0d1018"   # Frontend Hosting
C_BFF_S    = "#fde047"; C_BFF_B    = "#1f1a05"   # BFF (Security)
C_BHOST_S  = "#a855f7"; C_BHOST_B  = "#1a0a30"   # Backend Hosting
C_PG_S     = "#3b82f6"; C_PG_B     = "#0a1530"   # PostgreSQL
C_SUPA_S   = "#22c55e"; C_SUPA_B   = "#0a2218"   # Supabase
C_API_S    = "#fb7185"; C_API_B    = "#2a0d12"   # API externa
C_MOB_S    = "#10b981"; C_MOB_B    = "#072018"   # Mobile
C_BA_S     = "#475569"; C_BA_B     = "#0e1422"   # Backend Architecture container

# Camadas (cells) Backend Architecture
C_CTRL_S   = "#ef4444"; C_CTRL_B   = "#3a0d0d"
C_SVC_S    = "#ea580c"; C_SVC_B    = "#2a1208"
C_REPO_S   = "#a16207"; C_REPO_B   = "#1f1605"
C_MODEL_S  = "#2563eb"; C_MODEL_B  = "#0a1535"
C_DTO_S    = "#b91c1c"; C_DTO_B    = "#280808"

# Cores de tabelas (por domínio)
C_TBL_AUTH = "#ef4444"
C_TBL_PROD = "#f97316"
C_TBL_ORD  = "#22c55e"
C_TBL_MENU = "#3b82f6"
C_TBL_STK  = "#06b6d4"
C_TBL_CASH = "#a855f7"
C_TBL_FIN  = "#ec4899"
C_TBL_LOG  = "#64748b"

C_W        = "#f1f5f9"
C_TITLE    = "#fafafa"
C_SUB      = "#94a3b8"
C_DIM      = "#64748b"
C_ARR      = "#cbd5e1"

# ═══════════════════════════════════════════════════════════════════════════════
# HELPERS
# ═══════════════════════════════════════════════════════════════════════════════
el = []

def R(x, y, w, h, stroke, bg, sw=2, dash=False, rough=0):
    return {"id": uid(), "type": "rectangle",
            "x": x, "y": y, "width": w, "height": h,
            "angle": 0, "strokeColor": stroke, "backgroundColor": bg,
            "fillStyle": "solid", "strokeWidth": sw,
            "strokeStyle": "dashed" if dash else "solid",
            "roughness": rough, "opacity": 100,
            "groupIds": [], "frameId": None,
            "roundness": {"type": 3},
            "seed": 1, "version": 1, "versionNonce": 1,
            "isDeleted": False, "boundElements": None,
            "updated": 1, "link": None, "locked": False}

def E(x, y, w, h, stroke, bg, sw=2):
    return {"id": uid(), "type": "ellipse",
            "x": x, "y": y, "width": w, "height": h,
            "angle": 0, "strokeColor": stroke, "backgroundColor": bg,
            "fillStyle": "solid", "strokeWidth": sw, "strokeStyle": "solid",
            "roughness": 0, "opacity": 100,
            "groupIds": [], "frameId": None,
            "roundness": None,
            "seed": 1, "version": 1, "versionNonce": 1,
            "isDeleted": False, "boundElements": None,
            "updated": 1, "link": None, "locked": False}

def T(x, y, w, text, color, size=13, align="left", family=1):
    lines = text.count('\n') + 1
    h = max(size * 1.45 * lines, 18)
    return {"id": uid(), "type": "text",
            "x": x, "y": y, "width": w, "height": h,
            "angle": 0, "strokeColor": color, "backgroundColor": "transparent",
            "fillStyle": "solid", "strokeWidth": 1, "strokeStyle": "solid",
            "roughness": 0, "opacity": 100,
            "groupIds": [], "frameId": None,
            "roundness": None,
            "seed": 1, "version": 1, "versionNonce": 1,
            "isDeleted": False, "boundElements": None,
            "updated": 1, "link": None, "locked": False,
            "text": text, "fontSize": size, "fontFamily": family,
            "textAlign": align, "verticalAlign": "top",
            "containerId": None, "originalText": text,
            "autoResize": True, "lineHeight": 1.45}

def A(x, y, pts, color, sw=2, dash=False):
    xs = [0] + [p[0] for p in pts]
    ys = [0] + [p[1] for p in pts]
    return {"id": uid(), "type": "arrow",
            "x": x, "y": y,
            "width": (max(xs) - min(xs)) or 1,
            "height": (max(ys) - min(ys)) or 1,
            "angle": 0, "strokeColor": color, "backgroundColor": "transparent",
            "fillStyle": "solid", "strokeWidth": sw,
            "strokeStyle": "dashed" if dash else "solid",
            "roughness": 0, "opacity": 100,
            "groupIds": [], "frameId": None,
            "roundness": {"type": 2},
            "seed": 1, "version": 1, "versionNonce": 1,
            "isDeleted": False, "boundElements": None,
            "updated": 1, "link": None, "locked": False,
            "points": [[0, 0]] + pts, "lastCommittedPoint": None,
            "startBinding": None, "endBinding": None,
            "startArrowhead": None, "endArrowhead": "arrow"}

# ═══════════════════════════════════════════════════════════════════════════════
# TÍTULO
# ═══════════════════════════════════════════════════════════════════════════════
el.append(T(20, 18, 1400, "ServeFlow Sistema para Pequenos Restaurantes",
            C_TITLE, size=26, align="left", family=1))
el.append(T(20, 55, 1400,
            "Frontend (Vercel)  ▸  Security  ▸  Backend (Render)  ▸  PostgreSQL (Supabase)",
            C_SUB, size=12, align="left", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# USERS  (5 perfis)
# ═══════════════════════════════════════════════════════════════════════════════
el.append(T(40, 90, 100, "Users", C_TITLE, size=14, align="left", family=1))

def role_card(x, y, w, h, c_stroke, c_bg, letter, name, sub1, sub2):
    el.append(R(x, y, w, h, c_stroke, c_bg, sw=2))
    # avatar circle
    el.append(E(x+10, y+10, 36, 36, c_stroke, c_bg, sw=2))
    el.append(T(x+10, y+18, 36, letter, c_stroke, size=16, align="center", family=1))
    # texto
    el.append(T(x+54, y+12, w-60, name, c_stroke, size=14, align="left", family=1))
    el.append(T(x+54, y+30, w-60, sub1, C_W,      size=10, align="left", family=1))
    el.append(T(x+54, y+45, w-60, sub2, C_DIM,    size=9,  align="left", family=1))

role_card( 40, 114, 145, 70, C_ADMIN_S, C_ADMIN_B, "A", "Admin",      "Full CRUD",   "Master")
role_card(195, 114, 145, 70, C_GER_S,   C_GER_B,   "M", "Gerente",    "Gestão Total","Manager")
role_card(350, 114, 145, 70, C_COZ_S,   C_COZ_B,   "K", "Cozinha",    "KDS + Stock", "Cook")
role_card(505, 114, 145, 70, C_GAR_S,   C_GAR_B,   "W", "Garçom",     "Pedidos",     "Waiter")
role_card(660, 114, 145, 70, C_CXA_S,   C_CXA_B,   "C", "Caixa",      "Sessões",     "Cashier")

el.append(T(40, 188, 300, "▼ Web", C_DIM, size=10, align="left", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# FRONTEND HOSTING  (Vercel)  —  coluna esquerda
# ═══════════════════════════════════════════════════════════════════════════════
FE_X, FE_Y, FE_W, FE_H = 40, 215, 420, 380
el.append(R(FE_X, FE_Y, FE_W, FE_H, C_FE_S, C_FE_B, sw=2))
el.append(T(FE_X+14, FE_Y+12, FE_W-28, "Frontend Hosting", C_FE_S, size=15, align="left", family=1))
el.append(T(FE_X+14, FE_Y+38, FE_W-28, "▲ Vercel",         C_FE_S, size=22, align="left", family=1))
el.append(T(FE_X+14, FE_Y+72, FE_W-28, "Frontend Hosting", C_SUB,  size=11, align="left", family=1))

el.append(T(FE_X+24, FE_Y+102, FE_W-48,
    "• React 19\n• React Router DOM\n• TailwindCSS\n• Axios + JWT Refresh\n• Zustand + React Query",
    C_W, size=12, align="left", family=1))

# Inner sub-card (Front-end/Web detail)
el.append(R(FE_X+14, FE_Y+225, FE_W-28, 140, C_COZ_S, "#0a1124", sw=1))
el.append(T(FE_X+24, FE_Y+233, FE_W-48,
    "Front-end/Web (React Router DOM, React 19)",
    C_COZ_S, size=11, align="left", family=1))
el.append(T(FE_X+24, FE_Y+258, FE_W-48,
    "• Interface web responsiva\n• Login com auth por perfil (RBAC)\n• Painel adaptado por tipo de usuário\n• Integração API REST  +  STOMP/WS\n• Hosting Vercel (deploy contínuo)",
    C_W, size=11, align="left", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# MOBILE  (roadmap)
# ═══════════════════════════════════════════════════════════════════════════════
MO_X, MO_Y, MO_W, MO_H = 40, 615, 200, 90
el.append(R(MO_X, MO_Y, MO_W, MO_H, C_MOB_S, C_MOB_B, sw=2, dash=True))
el.append(T(MO_X+14, MO_Y+10, MO_W-28, "📱 Mobile",     C_MOB_S, size=14, align="left", family=1))
el.append(T(MO_X+14, MO_Y+36, MO_W-28, "React Native\nExpo SDK\n(roadmap)", C_W, size=10, align="left", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# BFF / SECURITY LAYER  (yellow)
# ═══════════════════════════════════════════════════════════════════════════════
BF_X, BF_Y, BF_W, BF_H = 490, 215, 230, 380
el.append(R(BF_X, BF_Y, BF_W, BF_H, C_BFF_S, C_BFF_B, sw=2))
el.append(T(BF_X+14, BF_Y+14, BF_W-28, "BFF / Security",        C_BFF_S, size=15, align="left", family=1))
el.append(T(BF_X+14, BF_Y+34, BF_W-28, "(Backend for Frontend)", C_SUB,   size=10, align="left", family=1))
el.append(T(BF_X+14, BF_Y+72, BF_W-28,
    "• JwtFilter\n   (OncePerRequest)\n\n"
    "• RateLimitFilter\n   (anti brute-force)\n\n"
    "• CORS Config\n\n"
    "• SecurityHeaders\n   (CSP, HSTS)\n\n"
    "• BCrypt Hash\n\n"
    "• Refresh Token\n   Rotation\n\n"
    "• RBAC  (5 perfis)",
    C_W, size=11, align="left", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# BACKEND HOSTING  (Render) — topo central
# ═══════════════════════════════════════════════════════════════════════════════
BH_X, BH_Y, BH_W, BH_H = 750, 105, 330, 140
el.append(R(BH_X, BH_Y, BH_W, BH_H, C_BHOST_S, C_BHOST_B, sw=2))
el.append(T(BH_X+14, BH_Y+10, BH_W-28, "Backend Hosting", C_BHOST_S, size=15, align="left", family=1))
el.append(T(BH_X+14, BH_Y+34, BH_W-28, "▲ Render",        C_BHOST_S, size=20, align="left", family=1))
el.append(T(BH_X+14, BH_Y+68, BH_W-28, "Backend Hosting", C_SUB,     size=10, align="left", family=1))
el.append(T(BH_X+14, BH_Y+92, BH_W-28,
    "• Java 21  +  Spring Boot 3.4.3\n• Auto-deploy GitHub  +  HTTPS",
    C_W, size=11, align="left", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# BACKEND ARCHITECTURE  (Layered grid) — central principal
# ═══════════════════════════════════════════════════════════════════════════════
BA_X, BA_Y, BA_W, BA_H = 750, 260, 540, 460
el.append(R(BA_X, BA_Y, BA_W, BA_H, C_BA_S, C_BA_B, sw=2))
el.append(T(BA_X+14, BA_Y+10, BA_W-28, "Backend Architecture (Layered)",
            C_TITLE, size=14, align="left", family=1))
el.append(T(BA_X+14, BA_Y+30, BA_W-28, "Clean Arch + DDD  |  /api  :8080",
            C_SUB, size=10, align="left", family=1))

# Cabeçalho de colunas
COL_W = 96; COL_GAP = 4
COL_X0 = BA_X + 16
HDR_Y  = BA_Y + 58
cols = [
    ("Controller", C_CTRL_S),
    ("Service",    C_SVC_S),
    ("Repository", C_REPO_S),
    ("Model",      C_MODEL_S),
    ("DTO",        C_DTO_S),
]
for i, (name, stroke) in enumerate(cols):
    cx = COL_X0 + i * (COL_W + COL_GAP)
    el.append(R(cx, HDR_Y, COL_W, 22, stroke, "#0a0a14", sw=1))
    el.append(T(cx, HDR_Y+3, COL_W, name, stroke, size=10, align="center", family=1))

# Linhas (cada linha = um módulo de negócio)
rows = [
    ("UserCtrl",      "UserSvc",       "UserRepo",       "User",       "UserDTO"),
    ("AuthCtrl",      "AuthSvc",       "TokenRepo",      "Token",      "AuthDTO"),
    ("ProductCtrl",   "ProductSvc",    "ProductRepo",    "Product",    "ProductDTO"),
    ("MenuCtrl",      "MenuSvc",       "MenuRepo",       "Menu",       "MenuDTO"),
    ("OrderCtrl",     "OrderSvc",      "OrderRepo",      "Order",      "OrderDTO"),
    ("KdsCtrl",       "OrderSvc",      "OrderRepo",      "OrderItem",  "KdsDTO"),
    ("StockCtrl",     "StockSvc",      "StockRepo",      "StockItem",  "StockDTO"),
    ("CashierCtrl",   "CashierSvc",    "CashRepo",       "CashSession","CashDTO"),
    ("FinancialCtrl", "FinancialSvc",  "FinanceRepo",    "Receivable", "FinanceDTO"),
    ("DashboardCtrl", "DashboardSvc",  "MultiRepo",      "Address VO", "DashDTO"),
]
ROW_Y0 = HDR_Y + 28
ROW_H  = 32
ROW_GAP = 2
for r, row in enumerate(rows):
    ry = ROW_Y0 + r * (ROW_H + ROW_GAP)
    for c, (text, (_, stroke)) in enumerate(zip(row,
            [(_, s) for _, s in [(n, st) for n, st in cols]])):
        cx = COL_X0 + c * (COL_W + COL_GAP)
        # cor da célula = cor da coluna
        col_stroke, col_bg = [
            (C_CTRL_S, C_CTRL_B),
            (C_SVC_S,  C_SVC_B),
            (C_REPO_S, C_REPO_B),
            (C_MODEL_S,C_MODEL_B),
            (C_DTO_S,  C_DTO_B),
        ][c]
        el.append(R(cx, ry, COL_W, ROW_H, col_stroke, col_bg, sw=1))
        el.append(T(cx+3, ry+8, COL_W-6, text, C_W, size=10, align="center", family=1))

# Total / resumo
SUMM_Y = ROW_Y0 + len(rows) * (ROW_H + ROW_GAP) + 6
el.append(T(BA_X+14, SUMM_Y, BA_W-28,
    "11 Controllers  •  11 Services  •  19 Repositories  •  23 Entities",
    C_SUB, size=10, align="center", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# POSTGRESQL info  (top-right of center)
# ═══════════════════════════════════════════════════════════════════════════════
PG_X, PG_Y, PG_W, PG_H = 1110, 105, 280, 200
el.append(R(PG_X, PG_Y, PG_W, PG_H, C_PG_S, C_PG_B, sw=2))
el.append(T(PG_X+14, PG_Y+10, PG_W-28, "● PostgreSQL", C_PG_S, size=15, align="left", family=1))
el.append(T(PG_X+14, PG_Y+34, PG_W-28, "Database",     C_SUB,  size=11, align="left", family=1))
el.append(T(PG_X+14, PG_Y+60, PG_W-28,
    "• Managed by Supabase\n• Connection Pooling\n• Row Level Security\n"
    "• Real-time Subscriptions\n• Automatic Backups\n• Point-in-time Recovery",
    C_W, size=11, align="left", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# SUPABASE  (right)
# ═══════════════════════════════════════════════════════════════════════════════
SB_X, SB_Y, SB_W, SB_H = 1110, 325, 280, 200
el.append(R(SB_X, SB_Y, SB_W, SB_H, C_SUPA_S, C_SUPA_B, sw=2))
el.append(T(SB_X+14, SB_Y+10, SB_W-28, "◆ Supabase", C_SUPA_S, size=15, align="left", family=1))
el.append(T(SB_X+14, SB_Y+38, SB_W-28,
    "• Auth Service\n• Storage Service\n• Edge Functions\n• Dashboard & Studio\n"
    "• Realtime API\n• Free tier  (500MB)",
    C_W, size=11, align="left", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# API ViaCEP  (bottom center)
# ═══════════════════════════════════════════════════════════════════════════════
API_X, API_Y, API_W, API_H = 970, 640, 100, 90
el.append(R(API_X, API_Y, API_W, API_H, C_API_S, C_API_B, sw=2))
el.append(T(API_X, API_Y+10, API_W, "API", C_API_S, size=22, align="center", family=1))
el.append(T(API_X, API_Y+44, API_W, "ViaCEP", C_W, size=11, align="center", family=1))
el.append(T(API_X, API_Y+62, API_W, "▲▼", C_API_S, size=14, align="center", family=1))
el.append(T(API_X-30, API_Y-20, API_W+60, "API ViaCEP", C_DIM, size=10, align="center", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# DATABASE TABLES  (far right grid)
# ═══════════════════════════════════════════════════════════════════════════════
DT_X, DT_Y, DT_W, DT_H = 1410, 90, 470, 720
el.append(R(DT_X-4, DT_Y-4, DT_W+8, DT_H+8, "#334155", "#0d1018", sw=1))
el.append(T(DT_X+8, DT_Y+8, DT_W-16, "Database Tables", C_TITLE, size=16, align="left", family=1))
el.append(T(DT_X+8, DT_Y+32, DT_W-16, "24 tabelas  •  organizadas por domínio", C_SUB, size=10, align="left", family=1))

def table_card(x, y, w, h, color, name, cols_list):
    el.append(R(x, y, w, h, color, "#0a0a14", sw=2))
    el.append(R(x, y, w, 22, color, color, sw=0))
    el.append(T(x+8, y+4, w-16, name, "#0a0a14", size=11, align="left", family=3))
    el.append(T(x+8, y+28, w-16, "\n".join(cols_list), C_W, size=9, align="left", family=3))

# Tabelas organizadas em 4 colunas × 6 linhas (24 tabelas)
# Agrupadas por domínio (cor = domínio)
TC_W = 108; TC_H = 110
TC_GX = 8;  TC_GY = 8
TC_X0 = DT_X + 8
TC_Y0 = DT_Y + 58

tables = [
    # ── Auth & Users ──
    ("users",                C_TBL_AUTH, ["id BIGSERIAL PK", "username UQ", "email UQ", "password",
                                          "role", "jobposition"]),
    ("refresh_tokens",       C_TBL_AUTH, ["id PK", "user_id FK", "token_hash UQ", "expires_at"]),
    ("password_reset_tokens",C_TBL_AUTH, ["id UUID PK", "username", "token(6)", "expires_at", "used"]),
    ("flyway_schema",        C_TBL_LOG,  ["installed_rank PK", "version", "description",
                                          "checksum"]),
    # ── Catálogo ──
    ("products",             C_TBL_PROD, ["id_product UUID PK", "name", "category", "brand",
                                          "price", "image_url", "active"]),
    ("product_recipes",      C_TBL_PROD, ["id_recipe UUID PK", "product_id UQ", "product_type",
                                          "preparation_mode"]),
    ("recipe_ingredients",   C_TBL_PROD, ["id_ingredient PK", "id_recipe FK", "stock_item_id",
                                          "quantity_per_unit", "unit", "validity"]),
    ("addresses",            C_TBL_PROD, ["id_address UUID PK", "cep", "street", "city",
                                          "state", "number"]),
    # ── Pedidos ──
    ("orders",               C_TBL_ORD,  ["id_order UUID PK", "customer_name", "type",
                                          "status", "payment_method", "id_address FK"]),
    ("order_items",          C_TBL_ORD,  ["id_order_item PK", "id_order FK", "product_id",
                                          "quantity", "unit_price", "observation"]),
    ("item_additionals",     C_TBL_ORD,  ["id_item_add PK", "id_order_item FK", "name",
                                          "quantity", "unit_price"]),
    ("menus",                C_TBL_MENU, ["id_menu UUID PK", "name", "status",
                                          "day_of_week", "shift", "active_order_id"]),
    # ── Menus & Stock ──
    ("menu_items",           C_TBL_MENU, ["id_menu_item PK", "id_menu FK", "product_id",
                                          "name", "price", "available"]),
    ("stock_items",          C_TBL_STK,  ["id_stock_item PK", "name", "unit",
                                          "current_quantity", "minimum_quantity",
                                          "status"]),
    ("stock_movements",      C_TBL_STK,  ["id_movement PK", "stock_item_id FK", "type",
                                          "quantity", "balance_after", "reason"]),
    ("stock_alerts",         C_TBL_STK,  ["id_alert PK", "stock_item_id FK", "current_qty",
                                          "minimum_qty", "resolved"]),
    # ── Caixa & Financeiro ──
    ("cash_sessions",        C_TBL_CASH, ["id UUID PK", "status", "initial_balance",
                                          "opened_at", "closed_at", "opened_by"]),
    ("cash_movements",       C_TBL_CASH, ["id UUID PK", "session_id FK", "type",
                                          "amount", "category", "origem"]),
    ("accounts_receivable",  C_TBL_FIN,  ["id UUID PK", "description", "due_date",
                                          "amount", "status", "received_at"]),
    ("accounts_payable",     C_TBL_FIN,  ["id UUID PK", "description", "due_date",
                                          "amount", "status", "supplier"]),
    # ── Auditoria ──
    ("financial_audit",      C_TBL_FIN,  ["id UUID PK", "entity_type", "entity_id",
                                          "action", "performed_by"]),
    ("access_log",           C_TBL_LOG,  ["id PK", "user_id", "ip", "endpoint",
                                          "http_method", "http_status"]),
    ("audit_log",            C_TBL_LOG,  ["id PK", "user_id", "action", "entity",
                                          "previous_data", "new_data"]),
    ("error_log",            C_TBL_LOG,  ["id PK", "message", "stacktrace",
                                          "service", "created_at"]),
]

for idx, (name, color, cols_list) in enumerate(tables):
    col = idx % 4
    row = idx // 4
    tx = TC_X0 + col * (TC_W + TC_GX)
    ty = TC_Y0 + row * (TC_H + TC_GY)
    table_card(tx, ty, TC_W, TC_H, color, name, cols_list[:6])

# ═══════════════════════════════════════════════════════════════════════════════
# LEGENDA  (rodapé esquerdo)
# ═══════════════════════════════════════════════════════════════════════════════
LG_X, LG_Y, LG_W, LG_H = 40, 725, 420, 120
el.append(R(LG_X, LG_Y, LG_W, LG_H, "#475569", "#0d1018", sw=1))
el.append(T(LG_X+14, LG_Y+10, LG_W-28, "Legend", C_TITLE, size=12, align="left", family=1))

legend = [
    ("▲", C_FE_S,    "Vercel",     "Frontend Hosting (React)"),
    ("●", C_SUPA_S,  "Supabase",   "PostgreSQL + Auth + Storage"),
    ("▲", C_BHOST_S, "Render",     "Backend Hosting (Spring Boot)"),
    ("◆", C_BFF_S,   "Security",   "JWT + Rate Limit + CORS"),
]
for i, (icon, color, name, desc) in enumerate(legend):
    ly = LG_Y + 32 + i * 22
    el.append(T(LG_X+14,  ly, 16,  icon, color, size=11, align="left", family=1))
    el.append(T(LG_X+34,  ly, 70,  name, color, size=11, align="left", family=1))
    el.append(T(LG_X+108, ly, 320, desc, C_W,   size=10, align="left", family=1))

# ═══════════════════════════════════════════════════════════════════════════════
# SETAS  (fluxos principais — sem cruzamentos)
# ═══════════════════════════════════════════════════════════════════════════════

# Users → Frontend Hosting  (down)
el.append(A(250, 195, [[0, 18]], C_DIM, dash=True))

# Frontend → BFF  (right)
el.append(A(465, 405, [[20, 0]], C_ARR))

# BFF → Backend Architecture  (right)
el.append(A(725, 405, [[20, 0]], C_BFF_S))

# Backend Architecture → PostgreSQL  (up-right)
el.append(A(1295, 360, [[-1, -160]], C_PG_S))

# Backend Architecture → Supabase  (right, to middle)
el.append(A(1295, 425, [[-1, 0]], C_SUPA_S))

# Supabase ↔ Database Tables  (right)
el.append(A(1395, 425, [[15, 0]], C_SUPA_S))

# Backend Hosting → Backend Architecture (down, deploy)
el.append(A(915, 245, [[10, 15]], C_BHOST_S, dash=True))

# Backend Architecture → API ViaCEP  (down)
el.append(A(1020, 720, [[0, -1]], C_API_S))

# ═══════════════════════════════════════════════════════════════════════════════
# OUTPUT
# ═══════════════════════════════════════════════════════════════════════════════
diagram = {
    "type": "excalidraw",
    "version": 2,
    "source": "https://excalidraw.com",
    "elements": el,
    "appState": {
        "viewBackgroundColor": C_BG,
        "gridSize": None,
        "scrollX": 0, "scrollY": 0,
        "zoom": {"value": 1}
    },
    "files": {}
}

with open(OUTPUT, "w", encoding="utf-8") as f:
    json.dump(diagram, f, ensure_ascii=False, indent=2)

print(f"Diagrama gerado: {OUTPUT}")
print(f"Elementos: {len(el)}")
