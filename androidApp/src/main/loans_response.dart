import 'package:loan_mobile/features/pagination_response.dart';

class LoansResponse {
  final bool success;
  final String message;
  final List<Loan> data;
  final Pagination pagination;

  LoansResponse({
    required this.success,
    required this.message,
    required this.data,
    required this.pagination,
  });

  factory LoansResponse.fromJson(Map<String, dynamic> json) {
    return LoansResponse(
      success: json['success'] as bool,
      message: json['message'] as String,
      data: (json['data'] as List<dynamic>)
          .map((loanJson) => Loan.fromJson(loanJson as Map<String, dynamic>))
          .toList(),
      pagination: Pagination.fromJson(
        json['pagination'] as Map<String, dynamic>,
      ),
    );
  }
}

class Loan {
  final String id;
  final String loanNumber;
  final String loanIdNo;
  final String branch;
  final DateTime loanCreatedAt;
  final LoanType loanType;
  final String customerId;
  final String? groupId;
  final String? businessLicense;
  final String? colateralName;
  final String? colateralImage;
  final String requestedAmount;
  final String interestRate;
  final int durationMonths;
  final LoanStatus status;
  final DateTime createdAt;
  final DateTime updatedAt;
  final Customer customer;
  final Group? group;
  final List<Repayment> repayments;
  final Summary summary;

  Loan({
    required this.id,
    required this.loanNumber,
    required this.loanIdNo,
    required this.branch,
    required this.loanCreatedAt,
    required this.loanType,
    required this.customerId,
    this.groupId,
    this.businessLicense,
    this.colateralName,
    this.colateralImage,
    required this.requestedAmount,
    required this.interestRate,
    required this.durationMonths,
    required this.status,
    required this.createdAt,
    required this.updatedAt,
    required this.customer,
    this.group,
    required this.repayments,
    required this.summary,
  });

  factory Loan.fromJson(Map<String, dynamic> json) {
    return Loan(
      id: json['id'] as String,
      loanNumber: json['loanNumber'] as String,
      loanIdNo: json['loanIdNo'] as String,
      branch: json['branch'] as String,
      loanCreatedAt: DateTime.parse(json['loanCreatedAt']),
      loanType: LoanTypeExtension.fromApi(json['loanType'] as String),
      customerId: json['customerId'] as String,
      groupId: json['groupId'] as String?,
      businessLicense: json['businessLicense'] as String?,
      colateralName: json['colateralName'] as String?,
      colateralImage: json['colateralImage'] as String?,
      requestedAmount: json['requestedAmount'] as String,
      interestRate: json['interestRate'] as String,
      durationMonths: json['durationMonths'] as int,
      status: LoanStatusExtension.fromApi(json['status'] as String),
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
      customer: Customer.fromJson(json['Customer'] as Map<String, dynamic>),
      group: json['Group'] != null
          ? Group.fromJson(json['Group'] as Map<String, dynamic>)
          : null,
      repayments: (json['repayments'] as List<dynamic>)
          .map(
            (repaymentJson) =>
                Repayment.fromJson(repaymentJson as Map<String, dynamic>),
          )
          .toList(),
      summary: Summary.fromJson(json['summary'] as Map<String, dynamic>),
    );
  }
}

class Customer {
  final String id;
  final String name;
  final String phone;
  final String nationalId;
  final String fatherName;
  final String? spouseName;
  final String work;
  final String address;
  final String frontNRCUrl;
  final String backNRCUrl;
  final String frontHouseholdListUrl;
  final String backHouseholdListUrl;

  Customer({
    required this.id,
    required this.name,
    required this.phone,
    required this.nationalId,
    required this.fatherName,
    this.spouseName,
    required this.work,
    required this.address,
    required this.frontNRCUrl,
    required this.backNRCUrl,
    required this.frontHouseholdListUrl,
    required this.backHouseholdListUrl,
  });

  factory Customer.fromJson(Map<String, dynamic> json) {
    return Customer(
      id: json['id'] as String,
      name: json['name'] as String,
      phone: json['phone'] as String,
      nationalId: json['nationalId'] as String,
      fatherName: json['fatherName'] as String,
      spouseName: json['spouseName'] as String?,
      work: json['work'] as String,
      address: json['address'] as String,
      frontNRCUrl: json['frontNRCUrl'] as String,
      backNRCUrl: json['backNRCUrl'] as String,
      frontHouseholdListUrl: json['frontHouseholdListUrl'] as String,
      backHouseholdListUrl: json['backHouseholdListUrl'] as String,
    );
  }
}

class Group {
  final String id;
  final String name;

  Group({required this.id, required this.name});

  factory Group.fromJson(Map<String, dynamic> json) {
    return Group(id: json['id'] as String, name: json['name'] as String);
  }
}

class Repayment {
  final String amountPaid;

  Repayment({required this.amountPaid});

  factory Repayment.fromJson(Map<String, dynamic> json) {
    return Repayment(amountPaid: json['amountPaid'] as String);
  }
}

class Summary {
  final int totalAmount;
  final int totalPaid;
  final int remainingBalance;

  Summary({
    required this.totalAmount,
    required this.totalPaid,
    required this.remainingBalance,
  });

  factory Summary.fromJson(Map<String, dynamic> json) {
    return Summary(
      totalAmount: json['totalAmountToPay'] as int,
      totalPaid: json['totalPaid'] as int,
      remainingBalance: json['remainingBalance'] as int,
    );
  }
}

class LoanFilter {
  final LoanType? loanType;
  final LoanStatus? loanStatus;
  final String? search;

  LoanFilter({this.loanStatus, this.loanType, this.search});

  LoanFilter copyWith({
    LoanType? loanType,
    LoanStatus? loanStatus,
    String? search,
  }) {
    return LoanFilter(
      loanType: loanType ?? this.loanType,
      loanStatus: loanStatus ?? this.loanStatus,
      search: search ?? this.search,
    );
  }

  Map<String, dynamic> toQueryParameters() {
    return {
      if (loanType != null) 'loanType': loanType!.apiValue,
      if (loanStatus != null) 'status': loanStatus!.apiValue,
      if (search != null && search!.isNotEmpty) 'search': search,
    };
  }
}

enum GroupRole { member, leader }

extension GroupRoleExtension on GroupRole {
  String get apiValue {
    switch (this) {
      case GroupRole.member:
        return 'MEMBER';
      case GroupRole.leader:
        return 'LEADER';
    }
  }

  static GroupRole fromApi(String value) {
    switch (value) {
      case 'MEMBER':
        return GroupRole.member;
      case 'LEADER':
        return GroupRole.leader;
      default:
        throw ArgumentError('Invalid group role value: $value');
    }
  }
}

enum LoanType { group, individual, business, special }

extension LoanTypeExtension on LoanType {
  String get apiValue {
    switch (this) {
      case LoanType.group:
        return 'GROUP';
      case LoanType.individual:
        return 'INDIVIDUAL';
      case LoanType.business:
        return 'BUSINESS';
      case LoanType.special:
        return 'SPECIAL';
    }
  }

  static LoanType fromApi(String value) {
    switch (value) {
      case 'GROUP':
        return LoanType.group;
      case 'INDIVIDUAL':
        return LoanType.individual;
      case 'BUSINESS':
        return LoanType.business;
      case 'SPECIAL':
        return LoanType.special;
      default:
        throw ArgumentError('Invalid loan type value: $value');
    }
  }
}

enum LoanStatus { pending, paid, overdue }

extension LoanStatusExtension on LoanStatus {
  String get apiValue {
    switch (this) {
      case LoanStatus.pending:
        return 'PENDING';
      case LoanStatus.paid:
        return 'PAID';
      case LoanStatus.overdue:
        return 'OVERDUE';
    }
  }

  static LoanStatus fromApi(String value) {
    switch (value) {
      case 'PENDING':
        return LoanStatus.pending;
      case 'PAID':
        return LoanStatus.paid;
      case 'OVERDUE':
        return LoanStatus.overdue;
      default:
        throw ArgumentError('Invalid loan status value: $value');
    }
  }
}

extension LoanStatusDisplay on LoanStatus {
  String get displayValue {
    switch (this) {
      case LoanStatus.pending:
        return 'Pending';
      case LoanStatus.paid:
        return 'Paid';
      case LoanStatus.overdue:
        return 'Overdue';
    }
  }
}

extension LoanTypeDisplay on LoanType {
  String get displayValue {
    switch (this) {
      case LoanType.group:
        return 'Group';
      case LoanType.individual:
        return 'Individual';
      case LoanType.business:
        return 'Business';
      case LoanType.special:
        return 'Special';
    }
  }
}
