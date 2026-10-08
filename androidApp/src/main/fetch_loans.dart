// import 'package:loan_mobile/features/loans/dummy_loans.dart';
// import 'package:loan_mobile/features/loans/loans_response.dart';
// import 'package:loan_mobile/helpers/api_client.dart';

// class LoansService {
//   final ApiClient apiClient;
//   LoansService(this.apiClient);

//   Future<LoansResponse> fetchLoans({
//     required LoanFilter filter,
//     int page = 1,
//     int limit = 20,
//   }) async {
//     final queryParameters = <String, dynamic>{
//       ...filter.toQueryParameters(),
//       'page': page,
//       'limit': limit,
//     };
//     final response = await apiClient.request(
//       path: '/api/loans',
//       method: 'GET',
//       queryParameters: queryParameters,
//     );
//     final loansJson = response.data;
//     return LoansResponse.fromJson(loansJson as Map<String, dynamic>);
//   }

//   Future<LoansResponse> fetchDummyLoans({
//     required LoanFilter filter,
//     int page = 1,
//     int limit = 20,
//   }) async {
//     await Future.delayed(const Duration(seconds: 5));
//     return LoansResponse.fromJson(dummyLoansResponse);
//   }
// }

import 'package:loan_mobile/features/loans/dummy_loans.dart';
import 'package:loan_mobile/features/loans/loans_response.dart';
import 'package:loan_mobile/helpers/api_client.dart';

class LoansService {
  final ApiClient apiClient;
  LoansService(this.apiClient);

  static const bool USE_DUMMY_DATA = false;

  Future<LoansResponse> fetchLoans({
    required LoanFilter filter,
    int page = 1,
    int limit = 20,
  }) async {
    //  DUMMY MODE
    if (USE_DUMMY_DATA) {
      await Future.delayed(const Duration(milliseconds: 500));

      final allLoans = List<Map<String, dynamic>>.from(
        (dummyLoansResponse['data'] as List).map(
          (e) => Map<String, dynamic>.from(e as Map),
        ),
      );

      var filtered = allLoans;

      // Filter: Loan Type
      final typeValue =
          filter.loanType?.apiValue ?? filter.loanType?.toString();
      if (typeValue != null && typeValue.isNotEmpty) {
        filtered = filtered.where((l) => l['loanType'] == typeValue).toList();
      }

      // Filter: Status
      final statusValue =
          filter.loanStatus?.apiValue ?? filter.loanStatus?.toString();
      if (statusValue != null && statusValue.isNotEmpty) {
        filtered = filtered.where((l) => l['status'] == statusValue).toList();
      }

      // Filter: Search (Null-safe)
      final q = filter.search?.trim().toLowerCase() ?? '';
      if (q.isNotEmpty) {
        filtered = filtered.where((l) {
          final customer = l['Customer'] as Map?;
          final name = (customer?['name'] as String? ?? '').toLowerCase();
          final nrc = (customer?['nationalId'] as String? ?? '').toLowerCase();
          final loanNo = (l['loanNumber'] as String? ?? '').toLowerCase();
          final loanId = (l['loanIdNo'] as String? ?? '').toLowerCase();
          return name.contains(q) ||
              nrc.contains(q) ||
              loanNo.contains(q) ||
              loanId.contains(q);
        }).toList();
      }

      // Pagination
      final start = (page - 1) * limit;
      final end = (start + limit).clamp(0, filtered.length);
      final paged = start >= filtered.length
          ? <Map<String, dynamic>>[]
          : filtered.sublist(start, end);

      // Build Response
      return LoansResponse.fromJson({
        "success": true,
        "message": "Dummy loans fetched",
        "data": paged,
        "pagination": {
          "total": filtered.length,
          "page": page,
          "limit": limit,
          "totalPages": (filtered.length / limit).ceil().clamp(1, 999),
        },
      });
    }

    // REAL API MODE
    final queryParameters = <String, dynamic>{
      ...filter.toQueryParameters(),
      'page': page,
      'limit': limit,
    };

    final response = await apiClient.request(
      path: '/api/loans',
      method: 'GET',
      queryParameters: queryParameters,
    );

    return LoansResponse.fromJson(response.data as Map<String, dynamic>);
  }
}
