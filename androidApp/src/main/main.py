from typing import List, Optional, Dict
from fastapi import FastAPI, Query, Header
from pydantic import BaseModel
from fastapi.responses import JSONResponse
from pydantic import Field
import uvicorn

app = FastAPI(
    title="KM Microfinance Test Server",
    description="Updated mock backend matching API.md specification for Kotlin Compose App",
    version="2.0.0"
)

# -----------------------------------------------------------------------------
# Pydantic Schemas matching API.md
# -----------------------------------------------------------------------------
class CustomerInfo(BaseModel):
    id: str
    name: str
    phone: Optional[str] = None
    nationalId: Optional[str] = None
    fatherName: Optional[str] = None
    spouseName: Optional[str] = None
    work: Optional[str] = None
    address: Optional[str] = None
    frontNRCUrl: Optional[str] = None
    backNRCUrl: Optional[str] = None
    frontHouseholdListUrl: Optional[str] = None
    backHouseholdListUrl: Optional[str] = None

class GroupInfo(BaseModel):
    id: str
    name: str

class LoanSummary(BaseModel):
    totalAmountToPay: float
    totalPaid: float
    remainingBalance: float

class LoanDto(BaseModel):
    id: str
    loanNumber: Optional[str] = None
    loanIdNo: Optional[str] = None
    branch: Optional[str] = "Magway"
    loanCreatedAt: Optional[str] = None
    customerId: Optional[str] = None
    groupId: Optional[str] = None
    businessLicence: Optional[str] = None
    collateralName: Optional[str] = None
    collateralImage: Optional[str] = None
    interestRate: Optional[str] = "2.5"
    durationMonths: Optional[int] = 12
    createdAt: Optional[str] = None
    updatedAt: Optional[str] = None
    loanType: Optional[str] = "INDIVIDUAL"
    requestedAmount: Optional[str] = "0"
    status: Optional[str] = "PENDING"
    Customer: Optional[CustomerInfo] = None
    Group: Optional[GroupInfo] = None
    summary: Optional[LoanSummary] = None
    repayments: List[dict] = Field(default_factory=list)
    groupMembers: List[dict] = Field(default_factory=list)

class PaginationInfo(BaseModel):
    total: int
    page: int
    limit: int
    totalPages: int

class LoansApiResponse(BaseModel):
    success: bool = True
    message: Optional[str] = "Loans fetched successfully"
    data: List[LoanDto]
    pagination: PaginationInfo

class LoanDetailApiResponse(BaseModel):
    success: bool = True
    message: str = "Loan fetched successfully"
    data: LoanDto

class RepaymentHistoryDto(BaseModel):
    id: str
    repaymentNo: Optional[str] = None
    amountPaid: float
    paymentMethod: Optional[str] = "CASH"
    paymentDate: Optional[str] = None
    notes: Optional[str] = None

class RepaymentHistoryApiResponse(BaseModel):
    success: bool = True
    data: List[RepaymentHistoryDto]

class LoginRequest(BaseModel):
    email: str
    password: str

class LoginResponse(BaseModel):
    status: str = "success"
    message: str = "User logged in successfully"
    token: str


# -----------------------------------------------------------------------------
# Mock Database (18 Test Records formatted to API.md specification)
# -----------------------------------------------------------------------------
MOCK_LOANS: List[LoanDto] = [
    LoanDto(
        id="loan-1", loanNumber="LN-00001", loanIdNo="KM-MGY 00001", loanType="GROUP", status="ACTIVE", requestedAmount="89000000", loanCreatedAt="2026-09-25T00:00:00.000Z",
        customerId="c-1", groupId="g-1", interestRate="2.5", durationMonths=15,
        Customer=CustomerInfo(id="c-1", name="Ko Kyaw", phone="0912345678", nationalId="9/MAMANA(N)123456", fatherName="U Ba", work="Farmer", address="Magway"),
        Group=GroupInfo(id="g-1", name="Ko Kyaw & Group"),
        summary=LoanSummary(totalAmountToPay=122375000.0, totalPaid=0.0, remainingBalance=122375000.0)
    ),
    LoanDto(
        id="loan-2", loanNumber="LN-00002", loanIdNo="KM-MGY 00002", loanType="BUSINESS", status="PAID", requestedAmount="10000000", loanCreatedAt="2026-09-24T00:00:00.000Z",
        customerId="c-2", businessLicence="MDY-112483030", interestRate="2.5", durationMonths=10,
        Customer=CustomerInfo(id="c-2", name="U Kyaw Zin", phone="0923456789", nationalId="8/MAMANA(N)309987", fatherName="U ZawZaw", spouseName="Daw Mama", work="Shop Owner", address="Demon Road, Mandalay", frontNRCUrl="/uploads/c-2-front-nrc.jpg", backNRCUrl="/uploads/c-2-back-nrc.jpg"),
        summary=LoanSummary(totalAmountToPay=12500000.0, totalPaid=12500000.0, remainingBalance=0.0)
    ),
    LoanDto(
        id="loan-3", loanNumber="LN-00003", loanIdNo="KM-MGY 00003", loanType="INDIVIDUAL", status="PENDING", requestedAmount="6000000", loanCreatedAt="2026-09-23T00:00:00.000Z",
        Customer=CustomerInfo(id="c-3", name="Kg Lay", phone="0934567890"),
        summary=LoanSummary(totalAmountToPay=7800000.0, totalPaid=500000.0, remainingBalance=7300000.0)
    ),
    LoanDto(
        id="loan-4", loanNumber="LN-00004", loanIdNo="KM-MGY 00004", loanType="INDIVIDUAL", status="ACTIVE", requestedAmount="5000000", loanCreatedAt="2026-09-22T00:00:00.000Z",
        Customer=CustomerInfo(id="c-4", name="Shin Kyan", phone="0945678901"),
        summary=LoanSummary(totalAmountToPay=6250000.0, totalPaid=0.0, remainingBalance=6250000.0)
    ),
    LoanDto(
        id="loan-5", loanNumber="LN-00005", loanIdNo="KM-MGY 00005", loanType="BUSINESS", status="PENDING", requestedAmount="4500000", loanCreatedAt="2026-09-21T00:00:00.000Z",
        Customer=CustomerInfo(id="c-5", name="Aung Aung", phone="0956789012"),
        summary=LoanSummary(totalAmountToPay=5500000.0, totalPaid=1000000.0, remainingBalance=4500000.0)
    ),
    LoanDto(
        id="loan-6", loanNumber="LN-00006", loanIdNo="KM-MGY 00006", loanType="SPECIAL", status="PENDING", requestedAmount="3000000", loanCreatedAt="2026-09-20T00:00:00.000Z",
        Customer=CustomerInfo(id="c-6", name="Mya Mya", phone="0967890123"),
        summary=LoanSummary(totalAmountToPay=3600000.0, totalPaid=500000.0, remainingBalance=3100000.0)
    ),
    LoanDto(
        id="loan-7", loanNumber="LN-00007", loanIdNo="KM-MGY 00007", loanType="INDIVIDUAL", status="ACTIVE", requestedAmount="1500000", loanCreatedAt="2026-09-19T00:00:00.000Z",
        Customer=CustomerInfo(id="c-7", name="Zaw Zaw", phone="0978901234"),
        summary=LoanSummary(totalAmountToPay=1800000.0, totalPaid=300000.0, remainingBalance=1500000.0)
    ),
    LoanDto(
        id="loan-8", loanNumber="LN-00008", loanIdNo="KM-MGY 00008", loanType="BUSINESS", status="ACTIVE", requestedAmount="15000000", loanCreatedAt="2026-09-18T00:00:00.000Z",
        Customer=CustomerInfo(id="c-8", name="Thida Aung", phone="0989012345"),
        summary=LoanSummary(totalAmountToPay=18000000.0, totalPaid=3000000.0, remainingBalance=15000000.0)
    ),
    LoanDto(
        id="loan-9", loanNumber="LN-00009", loanIdNo="KM-MGY 00009", loanType="GROUP", status="ACTIVE", requestedAmount="2400000", loanCreatedAt="2026-09-17T00:00:00.000Z",
        Customer=CustomerInfo(id="c-9", name="Hla Hla", phone="0990123456"),
        summary=LoanSummary(totalAmountToPay=2900000.0, totalPaid=500000.0, remainingBalance=2400000.0)
    ),
    LoanDto(
        id="loan-10", loanNumber="LN-00010", loanIdNo="KM-MGY 00010", loanType="INDIVIDUAL", status="PENDING", requestedAmount="8800000", loanCreatedAt="2026-09-16T00:00:00.000Z",
        Customer=CustomerInfo(id="c-10", name="Min Thu", phone="0911223344"),
        summary=LoanSummary(totalAmountToPay=10000000.0, totalPaid=1200000.0, remainingBalance=8800000.0)
    ),
    LoanDto(
        id="loan-11", loanNumber="LN-00011", loanIdNo="KM-MGY 00011", loanType="SPECIAL", status="ACTIVE", requestedAmount="500000", loanCreatedAt="2026-09-15T00:00:00.000Z",
        Customer=CustomerInfo(id="c-11", name="Su Su", phone="0922334455"),
        summary=LoanSummary(totalAmountToPay=600000.0, totalPaid=100000.0, remainingBalance=500000.0)
    ),
    LoanDto(
        id="loan-12", loanNumber="LN-00012", loanIdNo="KM-MGY 00012", loanType="BUSINESS", status="ACTIVE", requestedAmount="9200000", loanCreatedAt="2026-09-14T00:00:00.000Z",
        Customer=CustomerInfo(id="c-12", name="Nilar Win", phone="0933445566"),
        summary=LoanSummary(totalAmountToPay=11000000.0, totalPaid=1800000.0, remainingBalance=9200000.0)
    ),
    LoanDto(
        id="loan-13", loanNumber="LN-00013", loanIdNo="KM-MGY 00013", loanType="GROUP", status="ACTIVE", requestedAmount="11000000", loanCreatedAt="2026-09-13T00:00:00.000Z",
        Customer=CustomerInfo(id="c-13", name="Tun Tun", phone="0944556677"),
        summary=LoanSummary(totalAmountToPay=13000000.0, totalPaid=2000000.0, remainingBalance=11000000.0)
    ),
    LoanDto(
        id="loan-14", loanNumber="LN-00014", loanIdNo="KM-MGY 00014", loanType="INDIVIDUAL", status="PENDING", requestedAmount="1200000", loanCreatedAt="2026-09-12T00:00:00.000Z",
        Customer=CustomerInfo(id="c-14", name="Zin Mar", phone="0955667788"),
        summary=LoanSummary(totalAmountToPay=1500000.0, totalPaid=300000.0, remainingBalance=1200000.0)
    ),
    LoanDto(
        id="loan-15", loanNumber="LN-00015", loanIdNo="KM-MGY 00015", loanType="SPECIAL", status="ACTIVE", requestedAmount="3500000", loanCreatedAt="2026-09-11T00:00:00.000Z",
        Customer=CustomerInfo(id="c-15", name="Khaing Thin", phone="0966778899"),
        summary=LoanSummary(totalAmountToPay=4200000.0, totalPaid=700000.0, remainingBalance=3500000.0)
    ),
    LoanDto(
        id="loan-16", loanNumber="LN-00016", loanIdNo="KM-MGY 00016", loanType="BUSINESS", status="ACTIVE", requestedAmount="18400000", loanCreatedAt="2026-09-10T00:00:00.000Z",
        Customer=CustomerInfo(id="c-16", name="Phyo Wai", phone="0977889900"),
        summary=LoanSummary(totalAmountToPay=22000000.0, totalPaid=3600000.0, remainingBalance=18400000.0)
    ),
    LoanDto(
        id="loan-17", loanNumber="LN-00017", loanIdNo="KM-MGY 00017", loanType="GROUP", status="ACTIVE", requestedAmount="6900000", loanCreatedAt="2026-09-09T00:00:00.000Z",
        Customer=CustomerInfo(id="c-17", name="Aye Aye", phone="0988990011"),
        summary=LoanSummary(totalAmountToPay=8000000.0, totalPaid=1100000.0, remainingBalance=6900000.0)
    ),
    LoanDto(
        id="loan-18", loanNumber="LN-00018", loanIdNo="KM-MGY 00018", loanType="INDIVIDUAL", status="ACTIVE", requestedAmount="4100000", loanCreatedAt="2026-09-08T00:00:00.000Z",
        Customer=CustomerInfo(id="c-18", name="Myo Win", phone="0999001122"),
        summary=LoanSummary(totalAmountToPay=5000000.0, totalPaid=900000.0, remainingBalance=4100000.0)
    ),
]

MOCK_REPAYMENTS: Dict[str, List[RepaymentHistoryDto]] = {
    # KM-MGY 00002 (U Kyaw Zin) - Full 4-item history (Matches your screenshots)
    "loan-2": [
        RepaymentHistoryDto(
            id="rp-1",
            repaymentNo="RP-101",
            amountPaid=11550000.0,
            paymentMethod="CASH",
            paymentDate="15 Sep 2026",
            notes="done"
        ),
        RepaymentHistoryDto(
            id="rp-2",
            repaymentNo="RP-102",
            amountPaid=200000.0,
            paymentMethod="WAVE_PAY",
            paymentDate="15 Sep 2026",
            notes=None
        ),
        RepaymentHistoryDto(
            id="rp-3",
            repaymentNo="RP-103",
            amountPaid=700000.0,
            paymentMethod="KBZ_PAY",
            paymentDate="15 Sep 2026",
            notes=None
        ),
        RepaymentHistoryDto(
            id="rp-4",
            repaymentNo="RP-104",
            amountPaid=50000.0,
            paymentMethod="CASH",
            paymentDate="15 Sep 2026",
            notes=None
        )
    ],
    # KM-MGY 00003 (Kg Lay) - Single payment history
    "loan-3": [
        RepaymentHistoryDto(
            id="rp-5",
            repaymentNo="RP-105",
            amountPaid=500000.0,
            paymentMethod="CASH",
            paymentDate="15 Sep 2026",
            notes="First installment"
        )
    ],
    # KM-MGY 00005 (Aung Aung) - Mobile Wallet payment history
    "loan-5": [
        RepaymentHistoryDto(
            id="rp-6",
            repaymentNo="RP-106",
            amountPaid=1000000.0,
            paymentMethod="KBZ_PAY",
            paymentDate="18 Sep 2026",
            notes="Partial payment"
        )
    ]
}

MOCK_GROUP_MEMBERS: Dict[str, List[dict]] = {
    "loan-1": [
        {"id": "gm-1", "roleInGroup": "LEADER", "Customer": MOCK_LOANS[0].Customer.dict()},
        {"id": "gm-2", "roleInGroup": "MEMBER", "Customer": CustomerInfo(id="c-1a", name="U Myint Soe", phone="09770112233", nationalId="09/KAKATA(N)001234", fatherName="U Aye Myint", work="Farmer", address="Bogyoke Road, Magway").dict()},
        {"id": "gm-3", "roleInGroup": "MEMBER", "Customer": CustomerInfo(id="c-1b", name="Daw San San Nu", phone="09770223344", nationalId="09/KAKATA(N)002345", fatherName="U Hla Myint", work="Shop Owner", address="Cherry Street, Magway").dict()},
        {"id": "gm-4", "roleInGroup": "MEMBER", "Customer": CustomerInfo(id="c-1c", name="Daw Aye Aye Thin", phone="09770334455", nationalId="09/KAPATA(N)003456", fatherName="U Win Maung", work="Tailor", address="Thitsar Street, Magway").dict()},
        {"id": "gm-5", "roleInGroup": "MEMBER", "Customer": CustomerInfo(id="c-1d", name="U Zaw Min Lwin", phone="09770445566", nationalId="09/KAPATA(N)004567", fatherName="U Kyaw San", work="Driver", address="Aung Mingalar, Magway").dict()}
    ]
}

# Demo credentials and token for the local mock server. These match API (3).md.
DEMO_EMAIL = "admin2@gmail.com"
DEMO_PASSWORD = "admin123"
DEMO_TOKEN = "mock-km-microfinance-token"
OWNER_DEMO_EMAIL = "owner@demo.local"
OWNER_DEMO_PASSWORD = "Owner123!"
OWNER_DEMO_TOKEN = "mock-km-owner-token"
MOCK_USERS = [
    {"id": "mock-owner-1", "name": "Demo Owner", "email": OWNER_DEMO_EMAIL, "role": "OWNER", "isDeleted": False, "photoUrl": None, "createdAt": "2026-01-12T09:30:00Z", "updatedAt": "2026-08-14T07:18:53Z"},
    {"id": "mock-manager-1", "name": "Admin 2", "email": DEMO_EMAIL, "role": "MANAGER", "isDeleted": False, "photoUrl": None, "createdAt": "2026-02-15T10:00:00Z", "updatedAt": "2026-08-14T07:18:53Z"},
    {"id": "mock-staff-1", "name": "Aung Kyaw", "email": "aung.staff@example.com", "role": "STAFF", "isDeleted": False, "photoUrl": None, "createdAt": "2026-03-17T11:20:00Z", "updatedAt": "2026-08-14T07:18:53Z"},
    {"id": "mock-staff-2", "name": "Hnin Ei", "email": "hnin.staff@example.com", "role": "STAFF", "isDeleted": False, "photoUrl": None, "createdAt": "2026-04-02T06:40:00Z", "updatedAt": "2026-08-14T07:18:53Z"}
]


def mock_role(authorization: Optional[str]) -> Optional[str]:
    if authorization == f"Bearer {OWNER_DEMO_TOKEN}":
        return "OWNER"
    if authorization == f"Bearer {DEMO_TOKEN}":
        return "MANAGER"
    return None


# -----------------------------------------------------------------------------
# Endpoint 1: GET /api/loans (Matching API.md)
# -----------------------------------------------------------------------------
@app.post("/api/auth/login", response_model=LoginResponse)
async def login(request: LoginRequest):
    normalized_email = request.email.strip().lower()
    if normalized_email == DEMO_EMAIL and request.password == DEMO_PASSWORD:
        return LoginResponse(token=DEMO_TOKEN)
    if normalized_email == OWNER_DEMO_EMAIL and request.password == OWNER_DEMO_PASSWORD:
        return LoginResponse(token=OWNER_DEMO_TOKEN)
    return JSONResponse(status_code=401, content={"message": "Incorrect email or password."})


@app.post("/api/auth/logout")
async def logout(authorization: Optional[str] = Header(None)):
    if mock_role(authorization) is None:
        return JSONResponse(status_code=401, content={"message": "Authentication required."})
    return {"status": "success", "message": "Logged out successfully."}


@app.get("/api/users/me")
async def get_current_user(authorization: Optional[str] = Header(None)):
    role = mock_role(authorization)
    if role is None:
        return JSONResponse(status_code=401, content={"message": "Authentication required."})
    account = next(user for user in MOCK_USERS if user["role"] == role)
    return {
        "status": "success",
        "message": "Your profile fetched successfully",
        "user": {key: account[key] for key in ("id", "name", "email", "role", "photoUrl")}
    }


@app.get("/api/users")
async def get_users(
    authorization: Optional[str] = Header(None),
    search: Optional[str] = Query(None),
    page: int = Query(1, ge=1),
    limit: int = Query(50, ge=1, le=100)
):
    if mock_role(authorization) not in ("OWNER", "MANAGER"):
        return JSONResponse(status_code=403, content={"message": "You do not have permission to view users."})
    users = MOCK_USERS
    if search:
        query = search.strip().lower()
        users = [user for user in users if query in user["name"].lower() or query in user["email"].lower() or query in user["role"].lower()]
    total = len(users)
    start = (page - 1) * limit
    return {
        "status": "success",
        "users": users[start:start + limit],
        "meta": {"totalUserCount": total, "totalPages": max(1, (total + limit - 1) // limit), "currentPage": page, "limit": limit}
    }


@app.get("/api/users/{user_id}")
async def get_user(user_id: str, authorization: Optional[str] = Header(None)):
    if mock_role(authorization) not in ("OWNER", "MANAGER"):
        return JSONResponse(status_code=403, content={"message": "You do not have permission to view this user."})
    user = next((account for account in MOCK_USERS if account["id"] == user_id), None)
    if user is None:
        return JSONResponse(status_code=404, content={"message": "User not found."})
    return {"status": "success", "message": "User fetched successfully", "user": user}


@app.get("/api/loans", response_model=LoansApiResponse)
async def get_loans(
    search: Optional[str] = Query(None, description="Search query for customer name or loan ID"),
    loanType: Optional[str] = Query(None, description="Filter by loan type"),
    status: Optional[str] = Query(None, description="Filter by status"),
    page: Optional[int] = Query(1, ge=1, description="Page number (starts at 1)"),
    limit: Optional[int] = Query(10, ge=1, le=100, description="Items per page"),
    authorization: Optional[str] = Header(None)
):
    results = MOCK_LOANS

    # 1. Search filter
    if search:
        query = search.strip().lower()
        results = [
            item for item in results
            if (item.Customer and query in item.Customer.name.lower())
            or (item.loanIdNo and query in item.loanIdNo.lower())
        ]

    # 2. Loan Type filter
    if loanType and loanType.lower() != "all":
        results = [item for item in results if item.loanType and item.loanType.lower() == loanType.lower()]

    # 3. Status filter
    if status and status.lower() != "all":
        results = [item for item in results if item.status and item.status.lower() == status.lower()]

    total_items = len(results)
    page_num = page or 1
    limit_num = limit or 10
    total_pages = max(1, (total_items + limit_num - 1) // limit_num)

    # 4. Pagination slicing
    start_index = (page_num - 1) * limit_num
    end_index = start_index + limit_num
    paginated_data = results[start_index:end_index]

    return LoansApiResponse(
        success=True,
        message="Loans fetched successfully",
        data=paginated_data,
        pagination=PaginationInfo(
            total=total_items,
            page=page_num,
            limit=limit_num,
            totalPages=total_pages
        )
    )


@app.get("/api/loans/{loan_id}", response_model=LoanDetailApiResponse)
async def get_loan_details(
    loan_id: str,
    authorization: Optional[str] = Header(None)
):
    loan = next((item for item in MOCK_LOANS if item.id == loan_id), None)
    if loan is None:
        return JSONResponse(
            status_code=404,
            content={"success": False, "message": "Loan record doesn't exist."}
        )

    detail = loan.copy(update={
        "repayments": [item.dict() for item in MOCK_REPAYMENTS.get(loan_id, [])],
        "groupMembers": MOCK_GROUP_MEMBERS.get(loan_id, [])
    })
    return LoanDetailApiResponse(data=detail)


@app.get("/api/repayments")
async def get_repayments(
    page: int = Query(1, ge=1),
    limit: int = Query(10, ge=1, le=100),
    search: Optional[str] = Query(None),
    authorization: Optional[str] = Header(None)
):
    records = []
    for loan_id, repayments in MOCK_REPAYMENTS.items():
        loan = next((item for item in MOCK_LOANS if item.id == loan_id), None)
        if loan is None:
            continue
        for repayment in repayments:
            item = repayment.dict()
            item["Loan"] = {
                "id": loan.id,
                "loanNumber": loan.loanNumber,
                "status": loan.status,
                "loanType": loan.loanType,
                "Customer": loan.Customer.dict() if loan.Customer else None
            }
            records.append(item)

    if search:
        query = search.strip().lower()
        records = [item for item in records if
                   query in (item.get("repaymentNo") or "").lower()
                   or query in (item.get("Loan", {}).get("loanNumber") or "").lower()
                   or query in (item.get("Loan", {}).get("Customer") or {}).get("name", "").lower()]

    start = (page - 1) * limit
    return {
        "success": True,
        "data": records[start:start + limit],
        "pagination": {
            "total": len(records),
            "page": page,
            "limit": limit,
            "totalPages": max(1, (len(records) + limit - 1) // limit)
        }
    }


# -----------------------------------------------------------------------------
# Endpoint 2: GET /api/repayments/loan/{loan_id} (Matching API.md)
# -----------------------------------------------------------------------------
# FastAPI (main.py)
@app.get("/api/repayments/loan/{loan_id}", response_model=RepaymentHistoryApiResponse)
async def get_repayment_history(
    loan_id: str,
    authorization: Optional[str] = Header(None)
):
    history = MOCK_REPAYMENTS.get(loan_id, [])
    return RepaymentHistoryApiResponse(
        success=True,
        data=history
    )


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
