from datetime import date

import pandas as pd
from fastapi import Depends, FastAPI, HTTPException, Query
from fastapi.responses import Response, StreamingResponse
from sqlalchemy.orm import Session

from app import analytics_service
from app.database import get_db

app = FastAPI(
    title="Bank Analytics Service",
    description="Python Enhancement Layer - transaction analytics",
    version="2.0.0",
)


@app.get("/health")
def health():
    return {"status": "ok", "service": "analytics-service"}


@app.get("/report")
def report():
    return {
        "message": "Transaction analytics service"
    }


@app.get("/analytics/monthly-report")
def monthly_report(
    year: int = Query(default=None),
    month: int = Query(default=None, ge=1, le=12),
    db: Session = Depends(get_db),
):
    today = date.today()
    report_year = year or today.year
    report_month = month or today.month
    return analytics_service.monthly_transaction_report(db, report_year, report_month)


@app.get("/analytics/spending-analysis")
def spending_analysis(
    user_id: int | None = Query(default=None),
    db: Session = Depends(get_db),
):
    return analytics_service.spending_analysis(db, user_id)


@app.get("/analytics/charts/monthly-spending")
def monthly_spending_chart(
    year: int = Query(default=None),
    db: Session = Depends(get_db),
):
    chart_year = year or date.today().year
    image = analytics_service.monthly_spending_chart(db, chart_year)
    return StreamingResponse(image, media_type="image/png")


@app.get("/analytics/charts/spending-by-user")
def spending_by_user_chart(db: Session = Depends(get_db)):
    image = analytics_service.spending_by_user_chart(db)
    return StreamingResponse(image, media_type="image/png")


@app.get("/analytics/export/monthly-csv")
def export_monthly_csv(
    year: int = Query(default=None),
    month: int = Query(default=None, ge=1, le=12),
    db: Session = Depends(get_db),
):
    today = date.today()
    export_year = year or today.year
    export_month = month or today.month
    csv_content = analytics_service.export_monthly_csv(db, export_year, export_month)
    filename = f"transactions_{export_year}_{export_month:02d}.csv"
    return Response(
        content=csv_content,
        media_type="text/csv",
        headers={"Content-Disposition": f'attachment; filename="{filename}"'},
    )
