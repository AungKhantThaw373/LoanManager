from typing import List, Optional, Dict
from fastapi import FastAPI, Query, Header
from pydantic import BaseModel
from fastapi.responses import JSONResponse
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
    loanType: Optional[str] = "INDIVIDUAL"
    requestedAmount: Optional[str] = "0"
    status: Optional[str] = "PENDING"
    Customer: Optional[CustomerInfo] = None
    Group: Optional[GroupInfo] = None
    summary: Optional[LoanSummary] = None

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
        Customer=CustomerInfo(id="c-1", name="Ko Kyaw", phone="0912345678"),
        Group=GroupInfo(id="g-1", name="Ko Kyaw & Group"),
        summary=LoanSummary(totalAmountToPay=122375000.0, totalPaid=0.0, remainingBalance=122375000.0)
    ),
    LoanDto(
        id="loan-2", loanNumber="LN-00002", loanIdNo="KM-MGY 00002", loanType="BUSINESS", status="PAID", requestedAmount="10000000", loanCreatedAt="2026-09-24T00:00:00.000Z",
        Customer=CustomerInfo(id="c-2", name="U Kyaw Zin", phone="0923456789"),
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

# Demo credentials and token for the local mock server. These match API (3).md.
DEMO_EMAIL = "admin2@gmail.com"
DEMO_PASSWORD = "admin123"
DEMO_TOKEN = "mock-km-microfinance-token"


# -----------------------------------------------------------------------------
# Endpoint 1: GET /api/loans (Matching API.md)
# -----------------------------------------------------------------------------
@app.post("/api/auth/login", response_model=LoginResponse)
async def login(request: LoginRequest):
    if request.email.strip().lower() != DEMO_EMAIL or request.password != DEMO_PASSWORD:
        return JSONResponse(
            status_code=401,
            content={"message": "Incorrect email or password."}
        )

    return LoginResponse(token=DEMO_TOKEN)


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
